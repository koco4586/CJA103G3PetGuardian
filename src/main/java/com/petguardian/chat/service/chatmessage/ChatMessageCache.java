package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.dto.ChatMessageRedisDTO;
import com.petguardian.chat.service.RedisJsonMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChatMessageCache {

    private static final String HISTORY_KEY = "chat:room:%d:history";
    private static final String WARMED_KEY = "chat:room:%d:warmed";
    private static final String QUEUE_KEY = "chat:write_queue:%d";
    private static final String RECENT_LOG_HASH = "chat:recent_msgs";
    private static final String DIRTY_ROOMS_SET = "chat:dirty_rooms";
    private static final String CIRCUIT_NAME = "redisCacheCircuit";
    private static final long DEFAULT_TTL_HOURS = 24;

    private final RedisJsonMapper redisJsonMapper;
    private final CircuitBreaker circuitBreaker;

    // Lua Script for Atomic ZADD (Score 0), Trim (by Rank), and Expire
    private static final String PUSH_TRIM_ZSET_LUA = "redis.call('ZADD', KEYS[1], 0, ARGV[1])\n" +
            "redis.call('ZREMRANGEBYRANK', KEYS[1], 0, - (ARGV[2] + 1))\n" +
            "redis.call('EXPIRE', KEYS[1], ARGV[3])\n" +
            "return 1";

    // Result is Long
    private final RedisScript<Long> pushTrimScript = new DefaultRedisScript<>(PUSH_TRIM_ZSET_LUA, Long.class);

    public ChatMessageCache(
            RedisJsonMapper redisJsonMapper,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.redisJsonMapper = redisJsonMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_NAME);
    }

    // =================================================================================
    // MESSAGE HISTORY (List)
    // =================================================================================

    public List<ChatMessageRedisDTO> getHistory(Integer roomId, int limit) {
        String key = String.format(HISTORY_KEY, roomId);
        try {
            return circuitBreaker.executeSupplier(() -> {
                // Lexicographical ZSET: Score is 0, so sorted by TSID (JSON String)
                // TSID is Base32 (sortable string), so reverse range gives newest messages
                Set<String> jsonSet = redisJsonMapper.getStringTemplate().opsForZSet()
                        .reverseRange(key, 0, limit - 1);

                if (jsonSet == null || jsonSet.isEmpty()) {
                    return Collections.emptyList();
                }

                return jsonSet.stream()
                        .map(member -> {
                            // Format: "TSID:JSON"
                            int delimiterIndex = member.indexOf(':');
                            if (delimiterIndex == -1)
                                return null; // Should not happen with new logic
                            String json = member.substring(delimiterIndex + 1);
                            return redisJsonMapper.fromJson(json, ChatMessageRedisDTO.class);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache read for room history {}.", circuitBreaker.getState(), roomId);
            return null; // Signal fallback to DB
        } catch (Exception e) {
            log.debug("[Cache] Failed to read history for room {}: {}", roomId, e.getMessage());
            return null; // Signal fallback to DB
        }
    }

    public void pushToHistory(Integer roomId, ChatMessageRedisDTO message, int limit) {
        String key = String.format(HISTORY_KEY, roomId);
        try {
            // Explicit serialization to JSON String for Lua
            // Prefix with TSID for correct lexicographical sorting
            String jsonMessage = message.messageId() + ":" + redisJsonMapper.toJson(message);

            circuitBreaker.executeRunnable(() -> redisJsonMapper.getStringTemplate().execute(
                    pushTrimScript,
                    Collections.singletonList(key),
                    jsonMessage,
                    String.valueOf(limit), // Lua args are strings
                    String.valueOf(Duration.ofHours(DEFAULT_TTL_HOURS).toSeconds())));
        } catch (CallNotPermittedException e) {
            throw new RuntimeException("Circuit Breaker Open", e);
        } catch (Exception e) {
            log.debug("[Cache] Failed to push message to history for room {}: {}", roomId, e.getMessage());
            throw new RuntimeException("Redis Push failed", e);
        }
    }

    public void warmUpHistory(Integer roomId, List<ChatMessageRedisDTO> messages) {
        String key = String.format(HISTORY_KEY, roomId);
        String warmedKey = String.format(WARMED_KEY, roomId);

        try {
            circuitBreaker.executeRunnable(() -> {
                if (!messages.isEmpty()) {
                    // Batch ZADD
                    Set<TypedTuple<String>> tuples = messages
                            .stream()
                            .map(msg -> TypedTuple.of(
                                    msg.messageId() + ":" + redisJsonMapper.toJson(msg),
                                    0.0))
                            .collect(Collectors.toSet());

                    redisJsonMapper.getStringTemplate().opsForZSet().add(key, tuples);
                    redisJsonMapper.expire(key, Duration.ofHours(DEFAULT_TTL_HOURS));
                }

                // Authority Marker: Set ONLY after merge
                redisJsonMapper.getStringTemplate().opsForValue().set(
                        warmedKey, "1", Duration.ofHours(DEFAULT_TTL_HOURS));
            });
        } catch (Exception e) {
            log.warn("[Cache] Failed to warm history for room {}: {}", roomId, e.getMessage());
            throw new RuntimeException("Cache warmup failed", e);
        }
    }

    public void invalidateHistory(Integer roomId) {
        try {
            circuitBreaker.executeRunnable(() -> {
                redisJsonMapper.delete(String.format(HISTORY_KEY, roomId));
                redisJsonMapper.delete(String.format(WARMED_KEY, roomId));
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping history invalidation for room {}.", circuitBreaker.getState(),
                    roomId);
            throw new RuntimeException("Circuit Breaker Open", e);
        } catch (Exception e) {
            log.debug("[Cache] Failed to invalidate history for room {}: {}", roomId, e.getMessage());
            throw new RuntimeException("Invalidation failed", e);
        }
    }

    public boolean isWarmed(Integer roomId) {
        try {
            return circuitBreaker.executeSupplier(() -> Boolean.TRUE.equals(
                    redisJsonMapper.getStringTemplate().hasKey(String.format(WARMED_KEY, roomId))));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Assuming unwarmed for room {}.", circuitBreaker.getState(), roomId);
            return false;
        } catch (Exception e) {
            log.warn("[Cache] Failed to check warmed status for room {}: {}", roomId, e.getMessage());
            return false;
        }
    }

    // =================================================================================
    // PERSISTENCE QUEUE (List)
    // =================================================================================

    public void enqueueForPersistence(int shardId, ChatMessageRedisDTO context) {
        String key = String.format(QUEUE_KEY, shardId);
        try {
            circuitBreaker.executeRunnable(() ->
            // Push JSON String
            redisJsonMapper.getStringTemplate().opsForList().leftPush(key, redisJsonMapper.toJson(context)));
        } catch (CallNotPermittedException e) {
            // If CB is open, we can't enqueue.
            throw new RuntimeException("Redis unavailable (CB Open)", e);
        } catch (Exception e) {
            log.debug("[Cache] Failed to enqueue for persistence (Shard {}): {}", shardId, e.getMessage());
            throw new RuntimeException("Redis enqueue failed", e);
        }
    }

    public List<ChatMessageRedisDTO> pollPersistenceBatch(int shardId, int batchSize) {
        String key = String.format(QUEUE_KEY, shardId);

        try {
            return circuitBreaker.executeSupplier(() -> {
                List<ChatMessageRedisDTO> batch = new ArrayList<>();
                // This is a loop of pops. Can be optimized with LPOP count in newer Redis, but
                // we use rightPop loop for now.
                for (int i = 0; i < batchSize; i++) {
                    String jsonItem = redisJsonMapper.getStringTemplate().opsForList().rightPop(key);
                    if (jsonItem != null) {
                        ChatMessageRedisDTO ctx = redisJsonMapper.fromJson(jsonItem, ChatMessageRedisDTO.class);
                        if (ctx != null) {
                            batch.add(ctx);
                        }
                    } else {
                        break;
                    }
                }
                return batch;
            });
        } catch (CallNotPermittedException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            log.debug("[Cache] Failed to poll persistence batch (Shard {}): {}", shardId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void requeuePersistenceBatch(int shardId, List<ChatMessageRedisDTO> batch) {
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

    public void putRecentLog(String messageId, ChatMessageRedisDTO message) {
        try {
            // Hash needs Strings
            String jsonMessage = redisJsonMapper.toJson(message);
            redisJsonMapper.getStringTemplate().opsForHash().put(RECENT_LOG_HASH, messageId, jsonMessage);
            redisJsonMapper.expire(RECENT_LOG_HASH, Duration.ofHours(DEFAULT_TTL_HOURS));
        } catch (Exception e) {
            log.debug("[Cache] Failed to put recent log: {}", e.getMessage());
            throw new RuntimeException("Cache put failed", e);
        }
    }

    public Integer popDirtyRoom() {
        try {
            return circuitBreaker.executeSupplier(() -> {
                String id = redisJsonMapper.getStringTemplate().opsForSet().pop(DIRTY_ROOMS_SET);
                if (id != null) {
                    return Integer.parseInt(id);
                }
                return null;
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping popDirtyRoom.", circuitBreaker.getState());
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
            throw new RuntimeException("Cache mark dirty failed", e);
        }
    }
}
