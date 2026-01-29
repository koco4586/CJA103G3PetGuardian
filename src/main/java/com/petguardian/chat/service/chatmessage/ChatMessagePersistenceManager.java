package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

/**
 * Internal Worker for Message Persistence.
 * Handles "Redis Buffer First" strategy with "MySQL Fallback".
 * Package-Private: Should only be accessed by ChatMessageService.
 */
@Slf4j
@Component
class ChatMessagePersistenceManager {

    // Redis Constraints
    private static final String ROOM_MSGS_Key = "chat:room:%d:msgs";
    private static final String RECENT_MSGS_HASH = "chat:recent_msgs";
    private static final String QUEUE_PREFIX = "chat:write_queue:"; // Internal Queue Key
    private static final int SHARD_COUNT = 10;
    private static final int CACHE_LIMIT = 50;
    private static final long CACHE_TTL_SECONDS = 86400;

    // Lua Script for Atomic Push/Trim
    private static final String PUSH_TRIM_EXPIRE_LUA = "redis.call('LPUSH', KEYS[1], ARGV[1])\n" +
            "redis.call('LTRIM', KEYS[1], 0, ARGV[2] - 1)\n" +
            "redis.call('EXPIRE', KEYS[1], ARGV[3])\n" +
            "return redis.call('LLEN', KEYS[1])";

    private static final RedisScript<Long> PUSH_TRIM_EXPIRE_SCRIPT = new DefaultRedisScript<>(PUSH_TRIM_EXPIRE_LUA,
            Long.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageRepository mysqlRepository;
    private final ChatRoomMetadataService metadataService;

    public ChatMessagePersistenceManager(
            @Lazy RedisTemplate<String, Object> redisTemplate,
            ChatMessageRepository mysqlRepository,
            ChatRoomMetadataService metadataService) {
        this.redisTemplate = redisTemplate;
        this.mysqlRepository = mysqlRepository;
        this.metadataService = metadataService;
    }

    /**
     * Dispatches the write operation.
     * Tries Redis Write-Behind first. If that fails (Exception), falls back to
     * MySQL.
     * Note: Resilience4j @CircuitBreaker is applied at the Facade level
     * (ChatMessageService).
     * This method contains the specific "Make it happen" logic.
     */
    ChatMessageEntity dispatchWrite(MessageCreationContext context) {
        // We attempt Redis path. If it throws, the Facade's Fallback should catch it
        // and call 'fallbackWriteToMysql'
        // But to keep this Manager self-contained for logic that MIGHT not trigger CB
        // (e.g. logic error),
        // we can also do try-catch here if we want strictly "Manager handles details".
        // HOWEVER, the design says Facade handles Resilience.
        // So here we implements the PRIMARY path. The Facade will call
        // fallbackWriteToMysql if this fails.
        return writeToRedis(context);
    }

    ChatMessageEntity fallbackWriteToMysql(MessageCreationContext context, Throwable t) {
        log.warn("[Persistence] Redis path failed ({}). Fallback to MySQL.", t.getMessage());
        ChatMessageEntity saved = mysqlRepository.save(context.toEntity());
        metadataService.syncRoomMetadata(context.chatroomId(), context.content(), context.createdAt(),
                context.senderId());
        return saved;
    }

    /**
     * Scheduled Task to Drain Buffer to MySQL (Write-Behind).
     * Replaces the old AsyncBatchPersistenceWorker.
     * Runs every 1 second.
     */
    private final java.util.concurrent.atomic.AtomicLong lastLogTime = new java.util.concurrent.atomic.AtomicLong(0);
    private static final long LOG_THROTTLE_MS = 60000;

    /**
     * Scheduled Task to Drain Buffer to MySQL (Write-Behind).
     * Replaces the old AsyncBatchPersistenceWorker.
     * Runs every 1 second.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 1000)
    public void scheduleBufferFlush() {
        // Simple sharded flush loop
        for (int i = 0; i < SHARD_COUNT; i++) {
            try {
                flushShard(i);
            } catch (Exception e) {
                // Throttled Logging to prevent flooding
                long now = System.currentTimeMillis();
                long last = lastLogTime.get();
                if (now - last > LOG_THROTTLE_MS) {
                    if (lastLogTime.compareAndSet(last, now)) {
                        log.error("[Persistence] Error flushing shard {}: {} (Suppressing for 60s)", i, e.getMessage());
                    }
                }
                // If one shard fails (likely Connection Refused), others will likely fail too.
                // Abort loop to save resources and logs.
                break;
            }
        }
    }

    private void flushShard(int shardId) {
        // Poll batch from buffer
        java.util.List<MessageCreationContext> batch = pollBatchInternal(shardId, 500);

        if (batch.isEmpty()) {
            return;
        }

        try {
            // Write to MySQL
            java.util.List<ChatMessageEntity> entities = batch.stream()
                    .map(MessageCreationContext::toEntity)
                    .collect(java.util.stream.Collectors.toList());

            mysqlRepository.saveAll(entities);
            log.debug("[Persistence] Flushed {} messages for shard {}", entities.size(), shardId);

            // Sync Metadata (Idempotent, so okay to redo if Redis path also did it)
            // Actually, Redis path already did it. But for safety in async-only path, we
            // might want to.
            // If Redis path FAILED (Fallback), the fallback method already did metadata
            // sync.
            // So strictly speaking, this flush is just for PERMANENCE of the message body.

        } catch (Exception e) {
            log.error("[Persistence] Failed to flush batch for shard {}. Re-queueing...", shardId);
            // Re-queue to retry later
            requeueInternal(shardId, batch);
        }
    }

    private ChatMessageEntity writeToRedis(MessageCreationContext context) {
        int shardId = context.chatroomId() % SHARD_COUNT;

        // 1. Buffer for Async Persistence (The "True" Persistence)
        offerInternal(shardId, context);

        // 2. Update Cache (The "Fast View")
        String cacheKey = String.format(ROOM_MSGS_Key, context.chatroomId());
        ChatMessageEntity entity = context.toEntity();

        Long currentSize = redisTemplate.execute(
                PUSH_TRIM_EXPIRE_SCRIPT,
                Collections.singletonList(cacheKey),
                entity,
                CACHE_LIMIT,
                CACHE_TTL_SECONDS);

        // 3. Update Recent Hash (for Reports/List View)
        redisTemplate.opsForHash().put(RECENT_MSGS_HASH, entity.getMessageId(), entity);
        redisTemplate.expire(RECENT_MSGS_HASH, Duration.ofSeconds(CACHE_TTL_SECONDS));

        log.debug("[Persistence] Saved to Redis Buffer & Cache. Size: {}", currentSize);

        // 4. Update Metadata
        metadataService.syncRoomMetadata(context.chatroomId(), entity.getMessage(), entity.getChatTime(),
                entity.getMemberId());

        return entity;
    }

    // ============================================================
    // INTERNAL BUFFER LOGIC (Merged from RedisMessageBufferService)
    // ============================================================

    private String getQueueKey(int shardId) {
        return QUEUE_PREFIX + shardId;
    }

    private void offerInternal(int shardId, MessageCreationContext context) {
        redisTemplate.opsForList().leftPush(getQueueKey(shardId), context);
    }

    private java.util.List<MessageCreationContext> pollBatchInternal(int shardId, int batchSize) {
        java.util.List<MessageCreationContext> batch = new java.util.ArrayList<>();
        String key = getQueueKey(shardId);

        // Blocking Pop for first item? No, Scheduled task should probably be
        // non-blocking or short blocking.
        // We use non-blocking RPOP loop specifically for @Scheduled.
        // Or if we want to mimic worker, we just drain current queue.

        for (int i = 0; i < batchSize; i++) {
            Object item = redisTemplate.opsForList().rightPop(key);
            if (item instanceof MessageCreationContext ctx) {
                batch.add(ctx);
            } else {
                break; // Queue Empty
            }
        }
        return batch;
    }

    private void requeueInternal(int shardId, java.util.List<MessageCreationContext> batch) {
        if (batch == null || batch.isEmpty())
            return;
        redisTemplate.opsForList().rightPushAll(getQueueKey(shardId), batch.toArray());
    }
}
