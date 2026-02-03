package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.service.RedisJsonMapper;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataCache;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataService;
import com.petguardian.chat.service.context.MessageCreationContext;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Internal Worker for Message Persistence.
 * Handles "Redis Buffer First" strategy with "MySQL Fallback".
 * Package-Private: Should only be accessed by ChatMessageService.
 */
@Slf4j
@Component
class ChatMessagePersistenceManager {

    private static final int SHARD_COUNT = 10;
    private static final int CACHE_LIMIT = 50;

    private final ChatMessageRepository mysqlRepository;
    private final ChatRoomRepository roomRepository;
    private final ChatRoomMetadataService metadataService;
    private final ChatRoomMetadataCache metadataCache;
    private final ChatMessageCache messageCache;
    private final RedisJsonMapper redisJsonMapper;
    private final TransactionTemplate transactionTemplate;
    private final CircuitBreaker circuitBreaker;

    private static final String CIRCUIT_NAME = "redisCacheCircuit";
    private static final int MESSAGE_FLUSH_BATCH_SIZE = 500;
    private static final int METADATA_SYNC_BATCH_SIZE = 50;

    public ChatMessagePersistenceManager(
            ChatMessageRepository mysqlRepository,
            ChatRoomRepository roomRepository,
            ChatRoomMetadataService metadataService,
            ChatRoomMetadataCache metadataCache,
            ChatMessageCache messageCache,
            RedisJsonMapper redisJsonMapper,
            TransactionTemplate transactionTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.mysqlRepository = mysqlRepository;
        this.roomRepository = roomRepository;
        this.metadataService = metadataService;
        this.metadataCache = metadataCache;
        this.messageCache = messageCache;
        this.redisJsonMapper = redisJsonMapper;
        this.transactionTemplate = transactionTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_NAME);
    }

    /**
     * Dispatches the write operation.
     */
    ChatMessageEntity dispatchWrite(MessageCreationContext context) {
        return writeToRedis(context);
    }

    ChatMessageEntity fallbackWriteToMysql(MessageCreationContext context, Throwable t) {
        log.warn("[Persistence] Redis path failed. Error: {}. Fallback to MySQL.", t.getMessage());
        try {
            ChatMessageEntity saved = mysqlRepository.save(context.toEntity());

            // 1. Sync cache (best effort - likely to fail if Redis is down)
            try {
                metadataService.syncRoomMetadata(context.chatroomId(), context.content(), context.createdAt(),
                        context.senderId());
            } catch (Exception e) {
                // Ignore, we expect this to fail
            }

            // 2. Changes: Mark this room as dirty for post-recovery invalidation
            // Since we can't update Redis now, we must delete the stale data later
            metadataCache.queueForRecovery(context.chatroomId());

            // 3. Sync DB (direct)
            transactionTemplate.executeWithoutResult(status -> {
                roomRepository.updateFullMetadata(
                        context.chatroomId(),
                        context.content(),
                        context.createdAt(),
                        context.createdAt(),
                        null);
            });
            return saved;
        } catch (Exception e) {
            log.error("[Persistence] Critical Fallback Failure: {}", e.getMessage(), e);
            throw e; // Reraise to trigger @Transactional rollback in Service
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleBufferFlush() {
        // No manual circuit breaker check. Rely on executeRunnable/Supplier to handle
        // state.

        // Piggyback Invalidation Logic
        // If Circuit Breaker is CLOSED (Redis Healthy) and we have pending
        // invalidations
        Set<Integer> pendingRooms = metadataCache.getRecoveryQueue();
        if (!pendingRooms.isEmpty() && circuitBreaker.getState() != CircuitBreaker.State.OPEN) {
            Iterator<Integer> it = pendingRooms.iterator();
            while (it.hasNext()) {
                Integer roomId = it.next();
                try {
                    // Try to invalidate (delete keys)
                    // If this succeeds, it means Redis is back up
                    circuitBreaker.executeRunnable(() -> {
                        messageCache.invalidateHistory(roomId);
                        metadataCache.invalidateRoom(roomId);
                    });

                    // Remove from set only if successful
                    it.remove();
                    log.info("[Recovery] Invalidated stale cache for room {}", roomId);
                } catch (Exception e) {
                    // Redis might still be unstable, or CB opened during loop
                    // Keep in set, retry next tick
                    log.debug("[Recovery] Failed to invalidate room {}, retrying later: {}", roomId, e.getMessage());
                }
            }
        }

        for (int i = 0; i < SHARD_COUNT; i++) {
            final int shardId = i;
            try {
                // Use CB to manage OPEN/HALF_OPEN/CLOSED transitions naturally
                circuitBreaker.executeRunnable(() -> flushShard(shardId));
            } catch (CallNotPermittedException e) {
                log.debug("[Persistence] Flush blocked by Circuit Breaker (state: {}). Skipping remaining shards.",
                        circuitBreaker.getState());
                break;
            } catch (Exception e) {
                // Use DEBUG level for all Redis connectivity issues to reduce log spam
                String msg = e.getMessage();
                if (msg != null && (msg.contains("timed out") || msg.contains("Unable to connect"))) {
                    log.debug("[Persistence] Redis unavailable on shard {}. CB will handle recovery.", shardId);
                } else {
                    log.debug("[Persistence] Error flushing shard {}: {}", shardId, msg);
                }
                break;
            }
        }

        try {
            circuitBreaker.executeRunnable(this::flushDirtyMetadata);
        } catch (CallNotPermittedException e) {
            log.debug("[Persistence] Metadata flush blocked by Circuit Breaker.");
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("timed out") || msg.contains("Unable to connect"))) {
                log.debug("[Persistence] Metadata flush: Redis unavailable. CB will handle recovery.");
            } else {
                log.debug("[Persistence] Metadata flush failed: {}", msg);
            }
        }
    }

    private void flushShard(int shardId) {
        // Fix: Use typed return from cache (List<MessageCreationContext>)
        List<MessageCreationContext> batch = messageCache.pollPersistenceBatch(shardId, MESSAGE_FLUSH_BATCH_SIZE);
        if (batch == null || batch.isEmpty())
            return;

        List<ChatMessageEntity> validEntities = new ArrayList<>();
        List<MessageCreationContext> validContexts = new ArrayList<>();

        // Fix: Process items individually to identify and drop poison messages
        for (MessageCreationContext ctx : batch) {
            try {
                if (ctx != null) {
                    ChatMessageEntity entity = ctx.toEntity();
                    if (entity != null) {
                        validEntities.add(entity);
                        validContexts.add(ctx);
                    }
                }
            } catch (Exception e) {
                log.error("[Persistence] Poison message discarded: {}", e.getMessage());
                // Do NOT requeue poison messages
            }
        }

        if (!validEntities.isEmpty()) {
            try {
                // Fix: Batch save
                mysqlRepository.saveAll(validEntities);
                log.info("[Persistence] Flushed {} messages to DB for shard {}", validEntities.size(), shardId);
            } catch (Exception e) {
                log.warn("[Persistence] Batch flush failed for shard {}. Fallback to granular processing. Error: {}",
                        shardId, e.getMessage());

                // Fix: Fallback to single-item processing to isolate poison messages
                List<MessageCreationContext> toRequeue = new ArrayList<>();

                for (int i = 0; i < validEntities.size(); i++) {
                    ChatMessageEntity entity = validEntities.get(i);
                    MessageCreationContext ctx = validContexts.get(i);
                    try {
                        mysqlRepository.save(entity);
                    } catch (DataIntegrityViolationException dive) {
                        log.error("[Persistence] Discarding POISON message id={} due to data error: {}",
                                entity.getMessageId(), dive.getMessage());
                    } catch (Exception ex) {
                        log.warn("[Persistence] Save failed for message id={}, adding to retry. Error: {}",
                                entity.getMessageId(), ex.getMessage());
                        toRequeue.add(ctx);
                    }
                }

                if (!toRequeue.isEmpty()) {
                    messageCache.requeuePersistenceBatch(shardId, toRequeue);
                }
            }
        }
    }

    private void flushDirtyMetadata() {
        Set<Integer> uniqueRoomIds = new HashSet<>();
        for (int i = 0; i < METADATA_SYNC_BATCH_SIZE; i++) {
            Integer roomId = messageCache.popDirtyRoom();
            if (roomId == null)
                break;
            uniqueRoomIds.add(roomId);
        }

        if (uniqueRoomIds.isEmpty())
            return;

        // Fix: N+1 issue. Batch update in a single transaction.
        try {
            transactionTemplate.executeWithoutResult(status -> {
                List<ChatRoomEntity> entities = roomRepository.findAllById(uniqueRoomIds);
                if (entities.isEmpty())
                    return;

                Map<Integer, ChatRoomEntity> entityMap = entities.stream()
                        .collect(Collectors.toMap(ChatRoomEntity::getChatroomId, e -> e));

                for (Integer roomId : uniqueRoomIds) {
                    metadataCache.getRoomMeta(roomId).ifPresent(dto -> {
                        ChatRoomEntity entity = entityMap.get(roomId);
                        if (entity != null) {
                            entity.setLastMessagePreview(dto.getLastMessagePreview());
                            entity.setLastMessageAt(dto.getLastMessageAt());
                            entity.setMem1LastReadAt(dto.getMem1LastReadAt());
                            entity.setMem2LastReadAt(dto.getMem2LastReadAt());
                        }
                    });
                }
                roomRepository.saveAll(entities);
            });
            log.debug("[Persistence] Batch synced metadata for rooms: {}", uniqueRoomIds);
        } catch (Exception e) {
            log.warn("[Persistence] Metadata batch sync failed: {}", e.getMessage());
        }
    }

    private ChatMessageEntity writeToRedis(MessageCreationContext context) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                int shardId = context.chatroomId() % SHARD_COUNT;

                // Fix: Use explicit JSON serialization
                String json = redisJsonMapper.toJson(context);

                try {
                    // Fix: Use getStringTemplate
                    redisJsonMapper.getStringTemplate().opsForList().leftPush(
                            String.format("chat:write_queue:%d", shardId), json);
                } catch (Exception e) {
                    throw new RuntimeException("Redis enqueue failed: " + e.getMessage(), e);
                }

                ChatMessageEntity entity = context.toEntity();

                // Fire-and-forget cache updates
                try {
                    messageCache.pushToHistory(context.chatroomId(), entity, CACHE_LIMIT);
                    messageCache.putRecentLog(entity.getMessageId(), entity);

                    metadataService.syncRoomMetadata(
                            context.chatroomId(),
                            entity.getMessage(),
                            entity.getChatTime(),
                            entity.getMemberId());
                } catch (Exception e) {
                    log.warn("[Persistence] partial cache update failed", e);
                }

                return entity;
            });
        } catch (CallNotPermittedException e) {
            // Map CB Open to failure for fallback
            throw new RuntimeException("Circuit Breaker is OPEN", e);
        }
    }
}
