package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.service.RedisJsonMapper;
import com.petguardian.chat.service.context.MessageCreationContext;
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

    // Lua Script for Atomic Push/Trim/Expire (Argv[1] is now implicitly string)
    private static final String PUSH_TRIM_EXPIRE_LUA = "redis.call('LPUSH', KEYS[1], ARGV[1])\n" +
            "redis.call('LTRIM', KEYS[1], 0, ARGV[2] - 1)\n" +
            "redis.call('EXPIRE', KEYS[1], ARGV[3])\n" +
            "return redis.call('LLEN', KEYS[1])";

    // Result is Long
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
            // Explicit serialization to JSON String for Lua
            String jsonMessage = redisJsonMapper.toJson(message);

            circuitBreaker.executeRunnable(() -> redisJsonMapper.getStringTemplate().execute(
                    pushTrimScript,
                    Collections.singletonList(key),
                    jsonMessage,
                    String.valueOf(limit), // Lua args are strings
                    String.valueOf(Duration.ofHours(DEFAULT_TTL_HOURS).toSeconds())));
        } catch (Exception e) {
            log.debug("[Cache] Failed to push message to history for room {}: {}", roomId, e.getMessage());
        }
    }

    public void warmUpHistory(Integer roomId, List<ChatMessageEntity> messages) {
        String key = String.format(HISTORY_KEY, roomId);
        try {
            circuitBreaker.executeRunnable(() -> {
                redisJsonMapper.delete(key);
                if (!messages.isEmpty()) {
                    // Convert all to JSON strings
                    List<String> jsonMessages = messages.stream()
                            .map(redisJsonMapper::toJson)
                            .toList();

                    redisJsonMapper.getStringTemplate().opsForList().rightPushAll(key, jsonMessages);
                    redisJsonMapper.expire(key, Duration.ofHours(DEFAULT_TTL_HOURS));
                }
            });
        } catch (Exception e) {
            log.debug("[Cache] Failed to warm history: {}", e.getMessage());
        }
    }

    public void invalidateHistory(Integer roomId) {
        String key = String.format(HISTORY_KEY, roomId);
        redisJsonMapper.delete(key);
    }

    // =================================================================================
    // PERSISTENCE QUEUE (List)
    // =================================================================================

    public void enqueueForPersistence(int shardId, MessageCreationContext context) {
        String key = String.format(QUEUE_KEY, shardId);
        try {
            // Push JSON String
            redisJsonMapper.getStringTemplate().opsForList().leftPush(key, redisJsonMapper.toJson(context));
        } catch (Exception e) {
            log.debug("[Cache] Failed to enqueue for persistence (Shard {}): {}", shardId, e.getMessage());
            // This is critical, caller might need to fail fallback to DB, but we keep the
            // current "void" contract
            // and let the caller handle exceptions if they propagate?
            // Current code caught it. Caller (PersistenceManager) manually threw?
            // Wait, this method swallows ex. PersistenceManager throws manually.
            // We should THROW here so PersistenceManager knows to fallback.
            throw new RuntimeException("Redis enqueue failed", e);
        }
    }

    // Returns Typed Objects now!
    public List<MessageCreationContext> pollPersistenceBatch(int shardId, int batchSize) {
        String key = String.format(QUEUE_KEY, shardId);
        List<MessageCreationContext> batch = new java.util.ArrayList<>();

        // This is a loop of pops. Can be optimized with LPOP count in newer Redis, but
        // we use rightPop loop for now.
        for (int i = 0; i < batchSize; i++) {
            String jsonItem = redisJsonMapper.getStringTemplate().opsForList().rightPop(key);
            if (jsonItem != null) {
                MessageCreationContext ctx = redisJsonMapper.fromJson(jsonItem, MessageCreationContext.class);
                if (ctx != null) {
                    batch.add(ctx);
                }
            } else {
                break;
            }
        }
        return batch;
    }

    public void requeuePersistenceBatch(int shardId, List<MessageCreationContext> batch) {
        String key = String.format(QUEUE_KEY, shardId);
        if (batch == null || batch.isEmpty())
            return;

        // Convert back to JSON strings
        List<String> jsonBatch = batch.stream()
                .map(redisJsonMapper::toJson)
                .toList();

        redisJsonMapper.getStringTemplate().opsForList().rightPushAll(key, jsonBatch);
    }

    // =================================================================================
    // MISC
    // =================================================================================

    public void putRecentLog(String messageId, ChatMessageEntity message) {
        try {
            // Hash needs Strings
            String jsonMessage = redisJsonMapper.toJson(message);
            redisJsonMapper.getStringTemplate().opsForHash().put(RECENT_LOG_HASH, messageId, jsonMessage);
            redisJsonMapper.expire(RECENT_LOG_HASH, Duration.ofHours(DEFAULT_TTL_HOURS));
        } catch (Exception e) {
            log.debug("[Cache] Failed to put recent log: {}", e.getMessage());
        }
    }

    public Integer popDirtyRoom() {
        try {
            String id = redisJsonMapper.getStringTemplate().opsForSet().pop(DIRTY_ROOMS_SET);
            if (id != null) {
                return Integer.parseInt(id);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void markRoomDirty(Integer roomId) {
        try {
            redisJsonMapper.getStringTemplate().opsForSet().add(DIRTY_ROOMS_SET, String.valueOf(roomId));
        } catch (Exception e) {
            log.debug("[Cache] Failed to mark room dirty: {}", e.getMessage());
        }
    }
}
