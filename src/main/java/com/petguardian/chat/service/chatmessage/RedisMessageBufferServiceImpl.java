package com.petguardian.chat.service.chatmessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis Implementation of the Message Buffer.
 * 
 * Uses Redis Lists (LPUSH/RPOP) to act as a scalable queue.
 * Implements Sharding Logic: chat:write_queue:{shardId}
 */
@Service
public class RedisMessageBufferServiceImpl implements MessageBufferService {

    private static final String QUEUE_PREFIX = "chat:write_queue:";
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisMessageBufferServiceImpl(@Lazy RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getQueueKey(int shardId) {
        return QUEUE_PREFIX + shardId;
    }

    @Override
    public void offer(int shardId, MessageCreationContext context) {
        // LPUSH: Adds to the head of the list
        redisTemplate.opsForList().leftPush(getQueueKey(shardId), context);
    }

    /**
     * Optimized Poll Batch.
     * 1. Blocking Pop for the first item (prevents CPU spin).
     * 2. Non-Blocking Pop for the rest of the batch (efficient bulk retrieval).
     */
    @Override
    public List<MessageCreationContext> pollBatch(int shardId, int batchSize, long timeoutMs) {
        List<MessageCreationContext> batch = new ArrayList<>();
        String key = getQueueKey(shardId);

        // 1. Wait for at least one item (Blocking)
        // BLPOP key timeout
        // Note: rightPop with timeout corresponds to BLPOP/BRPOP depending on
        // direction.
        // Since we LPUSH, we should RPOP (FIFO).
        Object firstItem = redisTemplate.opsForList().rightPop(key, timeoutMs, TimeUnit.MILLISECONDS);

        if (firstItem instanceof MessageCreationContext ctx) {
            batch.add(ctx);
        } else {
            return batch; // Timeout or empty
        }

        // 2. Drain the rest (Non-Blocking)
        // Can verify time remaining if needed, but for speed we usually just drain
        // available
        // up to batchLimit.
        while (batch.size() < batchSize) {
            Object next = redisTemplate.opsForList().rightPop(key);
            if (next instanceof MessageCreationContext nextCtx) {
                batch.add(nextCtx);
            } else {
                break; // Queue empty
            }
        }

        return batch;
    }

    @Override
    public long getQueueDepth(int shardId) {
        Long size = redisTemplate.opsForList().size(getQueueKey(shardId));
        return size != null ? size : 0;
    }

    @Override
    public void requeue(int shardId, List<MessageCreationContext> batch) {
        if (batch == null || batch.isEmpty())
            return;
        // Re-push to the Consumer side (Right) to be processed next
        // Since we polled from Right (FIFO), pushing back to Right puts them at the
        // front of the line
        redisTemplate.opsForList().rightPushAll(getQueueKey(shardId), batch.toArray());
    }

    @Override
    public boolean isHealthy() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
