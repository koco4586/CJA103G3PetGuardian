package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.service.context.MessageCreationContext;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataService;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataCache;
import com.petguardian.chat.service.RedisJsonMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

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

            // Sync cache (best effort)
            metadataService.syncRoomMetadata(context.chatroomId(), context.content(), context.createdAt(),
                    context.senderId());

            // Sync DB (direct)
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
        // Fail-fast: If CB is OPEN, skip the entire flush cycle
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            return; // Silent skip - no log spam
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
        List<Object> rawBatch = messageCache.pollPersistenceBatch(shardId, 500);
        if (rawBatch.isEmpty())
            return;

        try {
            List<ChatMessageEntity> entities = rawBatch.stream()
                    .map(item -> redisJsonMapper.convertValue(item, MessageCreationContext.class))
                    .filter(java.util.Objects::nonNull)
                    .map(MessageCreationContext::toEntity)
                    .collect(java.util.stream.Collectors.toList());

            if (!entities.isEmpty()) {
                mysqlRepository.saveAll(entities);
                log.info("[Persistence] Successfully flushed {} messages to DB for shard {}", entities.size(), shardId);
            } else {
                log.warn("[Persistence] Polled {} items but 0 valid messages for shard {}", rawBatch.size(), shardId);
            }

        } catch (Exception e) {
            log.error("[Persistence] Failed to flush batch for shard {}. Re-queueing... Error: {}", shardId,
                    e.getMessage());
            messageCache.requeuePersistenceBatch(shardId, rawBatch);
        }
    }

    private void flushDirtyMetadata() {
        for (int i = 0; i < 50; i++) {
            Integer roomId = messageCache.popDirtyRoom();
            if (roomId == null)
                break;
            syncRoomToDatabase(roomId);
        }
    }

    private void syncRoomToDatabase(Integer roomId) {
        metadataCache.getRoomMeta(roomId).ifPresentOrElse(
                dto -> {
                    transactionTemplate.executeWithoutResult(status -> {
                        roomRepository.updateFullMetadata(
                                roomId,
                                dto.getLastMessagePreview(),
                                dto.getLastMessageAt(),
                                dto.getMem1LastReadAt(),
                                dto.getMem2LastReadAt());
                    });
                    log.debug("[Persistence] Synced metadata for room {}", roomId);
                },
                () -> log.warn("[Persistence] Room metadata missing in cache for room {}", roomId));
    }

    private ChatMessageEntity writeToRedis(MessageCreationContext context) {
        // Fail-fast: If CB is OPEN, throw immediately to trigger fallback
        // This avoids waiting for Redis timeout (500ms)
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            throw new RuntimeException("Circuit Breaker is OPEN - Redis unavailable");
        }

        int shardId = context.chatroomId() % SHARD_COUNT;

        // CRITICAL: enqueueForPersistence MUST succeed, or we lose the message
        // We need to verify the enqueue operation succeeded
        try {
            redisJsonMapper.getTemplate().opsForList().leftPush(
                    String.format("chat:write_queue:%d", shardId), context);
        } catch (Exception e) {
            // Redis failed - throw to trigger CB fallback
            throw new RuntimeException("Redis enqueue failed: " + e.getMessage(), e);
        }

        ChatMessageEntity entity = context.toEntity();

        // These are optional - fire-and-forget
        messageCache.pushToHistory(context.chatroomId(), entity, CACHE_LIMIT);
        messageCache.putRecentLog(entity.getMessageId(), entity);

        metadataService.syncRoomMetadata(
                context.chatroomId(),
                entity.getMessage(),
                entity.getChatTime(),
                entity.getMemberId());

        return entity;
    }
}
