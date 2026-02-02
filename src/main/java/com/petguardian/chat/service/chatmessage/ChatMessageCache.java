package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.service.redis.RedisJsonMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Domain Cache for Chat Messages and Persistence Queues.
 * Uses RedisJsonMapper for type-safe storage in List structures.
 */
@Slf4j
@Component
public class ChatMessageCache {

    private static final String HISTORY_KEY = "chat:room:%d:msgs";
    private static final String QUEUE_KEY = "chat:write_queue:%d";
    private static final String RECENT_LOG_HASH = "chat:recent_msgs";
    private static final String DIRTY_ROOMS_SET = "chat:dirty_rooms";
    private static final String CIRCUIT_NAME = "redisCacheCircuit";
    private static final long DEFAULT_TTL_HOURS = 24;

    private final RedisJsonMapper redisJsonMapper;
    private final CircuitBreaker circuitBreaker;

    // Lua Script for Atomic Push/Trim/Expire
    private static final String PUSH_TRIM_EXPIRE_LUA = "redis.call('LPUSH', KEYS[1], ARGV[1])\n" +
            "redis.call('LTRIM', KEYS[1], 0, ARGV[2] - 1)\n" +
            "redis.call('EXPIRE', KEYS[1], ARGV[3])\n" +
            "return redis.call('LLEN', KEYS[1])";

    private final RedisScript<Long> pushTrimScript = new DefaultRedisScript<>(PUSH_TRIM_EXPIRE_LUA, Long.class);

    public ChatMessageCache(
            RedisJsonMapper redisJsonMapper,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.redisJsonMapper = redisJsonMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_NAME);
    }

    // =================================================================================
    // MESSAGE HISTORY (List)
    // =================================================================================

    public List<ChatMessageEntity> getHistory(Integer roomId, int limit) {
        String key = String.format(HISTORY_KEY, roomId);
        try {
            return circuitBreaker.executeSupplier(
                    () -> redisJsonMapper.getList(key, 0, limit - 1, ChatMessageEntity.class));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache read for room history {}.", circuitBreaker.getState(), roomId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.debug("[Cache] Failed to read history for room {}: {}", roomId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void pushToHistory(Integer roomId, ChatMessageEntity message, int limit) {
        String key = String.format(HISTORY_KEY, roomId);
        try {
            circuitBreaker.executeRunnable(() -> redisJsonMapper.getTemplate().execute(
                    pushTrimScript,
                    Collections.singletonList(key),
                    message,
                    limit,
                    Duration.ofHours(DEFAULT_TTL_HOURS).toSeconds()));
        } catch (Exception e) {
            log.debug("[Cache] Failed to push message to history for room {}: {}", roomId, e.getMessage());
        }
    }

    public void warmUpHistory(Integer roomId, List<ChatMessageEntity> messages) {
        String key = String.format(HISTORY_KEY, roomId);
        circuitBreaker.executeRunnable(() -> {
            redisJsonMapper.delete(key);
            if (!messages.isEmpty()) {
                redisJsonMapper.getTemplate().opsForList().rightPushAll(key, messages.toArray());
                redisJsonMapper.expire(key, Duration.ofHours(DEFAULT_TTL_HOURS));
            }
        });
    }

    // =================================================================================
    // PERSISTENCE QUEUE (List)
    // =================================================================================

    public void enqueueForPersistence(int shardId, Object context) {
        String key = String.format(QUEUE_KEY, shardId);
        try {
            redisJsonMapper.getTemplate().opsForList().leftPush(key, context);
        } catch (Exception e) {
            log.debug("[Cache] Failed to enqueue for persistence (Shard {}): {}", shardId, e.getMessage());
        }
    }

    public List<Object> pollPersistenceBatch(int shardId, int batchSize) {
        String key = String.format(QUEUE_KEY, shardId);
        List<Object> batch = new java.util.ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            Object item = redisJsonMapper.getTemplate().opsForList().rightPop(key);
            if (item != null)
                batch.add(item);
            else
                break;
        }
        return batch;
    }

    public void requeuePersistenceBatch(int shardId, List<?> batch) {
        String key = String.format(QUEUE_KEY, shardId);
        if (batch == null || batch.isEmpty())
            return;
        redisJsonMapper.getTemplate().opsForList().rightPushAll(key, batch.toArray());
    }

    public void putRecentLog(String messageId, ChatMessageEntity message) {
        try {
            redisJsonMapper.getTemplate().opsForHash().put(RECENT_LOG_HASH, messageId, message);
            redisJsonMapper.expire(RECENT_LOG_HASH, Duration.ofHours(DEFAULT_TTL_HOURS));
        } catch (Exception e) {
            log.debug("[Cache] Failed to put recent log: {}", e.getMessage());
        }
    }

    public Integer popDirtyRoom() {
        try {
            Object id = redisJsonMapper.getTemplate().opsForSet().pop(DIRTY_ROOMS_SET);
            if (id instanceof Number num)
                return num.intValue();
            if (id != null)
                return Integer.parseInt(id.toString());
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void markRoomDirty(Integer roomId) {
        try {
            redisJsonMapper.getTemplate().opsForSet().add(DIRTY_ROOMS_SET, roomId);
        } catch (Exception e) {
            log.debug("[Cache] Failed to mark room dirty: {}", e.getMessage());
        }
    }
}
