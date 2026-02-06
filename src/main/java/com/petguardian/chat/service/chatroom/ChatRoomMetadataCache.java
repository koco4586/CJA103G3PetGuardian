package com.petguardian.chat.service.chatroom;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.RedisJsonMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
public class ChatRoomMetadataCache {

    private static final String ROOM_KEY = "chat:room_meta:";
    private static final String MEMBER_KEY = "chat:member_meta:";
    private static final String REDIS_ROOM_LOOKUP_KEY = "chat:room_lookup:";
    private static final String REDIS_USER_ROOMS_KEY = "chat:user_rooms:";
    private static final String DIRTY_ROOMS_SET = "chat:dirty_rooms";
    private static final String CIRCUIT_NAME = "redisCacheCircuit";
    private static final long DEFAULT_TTL_DAYS = 1;

    // Track rooms that need cache invalidation after Redis recovery
    private final Set<Integer> pendingRecoveryRooms = ConcurrentHashMap.newKeySet();

    // Track user room lists that need cache invalidation after Redis recovery
    private final Set<Integer> pendingRecoveryUserRooms = ConcurrentHashMap.newKeySet();

    private final RedisJsonMapper redisJsonMapper;
    private final CircuitBreaker circuitBreaker;

    public ChatRoomMetadataCache(
            RedisJsonMapper redisJsonMapper,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.redisJsonMapper = redisJsonMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_NAME);
    }

    public boolean isCircuitBreakerOpen() {
        return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
    }

    // =================================================================================
    // READ OPERATIONS
    // =================================================================================

    public Optional<ChatRoomMetadataDTO> getRoomMeta(Integer roomId) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                String json = redisJsonMapper.getStringTemplate().opsForValue().get(ROOM_KEY + roomId);
                return Optional.ofNullable(redisJsonMapper.fromJson(json, ChatRoomMetadataDTO.class));
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache read for room {}.", circuitBreaker.getState(), roomId);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("[Cache] Failed to read room {}: {}", roomId, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<MemberProfileDTO> getMemberProfile(Integer memberId) {
        try {
            return circuitBreaker.executeSupplier(() -> {
                String json = redisJsonMapper.getStringTemplate().opsForValue().get(MEMBER_KEY + memberId);
                return Optional.ofNullable(redisJsonMapper.fromJson(json, MemberProfileDTO.class));
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache read for member {}.", circuitBreaker.getState(), memberId);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("[Cache] Failed to read member {}: {}", memberId, e.getMessage());
            return Optional.empty();
        }
    }

    public List<ChatRoomMetadataDTO> getRoomMetaBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return Collections.emptyList();

        try {
            return circuitBreaker.executeSupplier(() -> {
                List<String> keys = ids.stream().map(id -> ROOM_KEY + id).collect(Collectors.toList());
                // mget preserves order and nulls. We need to filter nulls to match original
                // contract
                // which returned only present items.
                List<ChatRoomMetadataDTO> results = redisJsonMapper.mget(keys, ChatRoomMetadataDTO.class);
                if (results == null)
                    return Collections.emptyList();

                return results.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping batch read for rooms.", circuitBreaker.getState());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("[Cache] Failed to batch read rooms: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<Integer, MemberProfileDTO> getMemberProfileBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return Collections.emptyMap();

        try {
            return circuitBreaker.executeSupplier(() -> {
                List<String> keys = ids.stream().map(id -> MEMBER_KEY + id).collect(Collectors.toList());
                List<MemberProfileDTO> results = redisJsonMapper.mget(keys, MemberProfileDTO.class);

                if (results == null || results.isEmpty() || results.size() != ids.size())
                    return Collections.emptyMap();

                // Correlate results back to IDs
                Map<Integer, MemberProfileDTO> map = new HashMap<>();
                for (int i = 0; i < ids.size(); i++) {
                    MemberProfileDTO dto = results.get(i);
                    if (dto != null) {
                        map.put(ids.get(i), dto);
                    }
                }
                return map;
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping batch read for members.", circuitBreaker.getState());
            return Collections.emptyMap();
        } catch (Exception e) {
            log.warn("[Cache] Failed to batch read members: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    // =================================================================================
    // WRITE OPERATIONS
    // =================================================================================

    public void setRoomMeta(Integer roomId, ChatRoomMetadataDTO dto) {
        try {
            circuitBreaker.executeRunnable(() -> {
                String json = redisJsonMapper.toJson(dto);
                redisJsonMapper.getStringTemplate().opsForValue().set(
                        ROOM_KEY + roomId,
                        json,
                        Duration.ofDays(DEFAULT_TTL_DAYS));
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache write for room {}.", circuitBreaker.getState(), roomId);
        } catch (Exception e) {
            log.warn("[Cache] Failed to cache room {}: {}", roomId, e.getMessage());
            throw new RuntimeException("Cache write failed", e);
        }
    }

    public void setMemberProfile(Integer memberId, MemberProfileDTO dto) {
        try {
            circuitBreaker.executeRunnable(() -> {
                String json = redisJsonMapper.toJson(dto);
                redisJsonMapper.getStringTemplate().opsForValue().set(
                        MEMBER_KEY + memberId,
                        json,
                        Duration.ofDays(DEFAULT_TTL_DAYS));
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache write for member {}.", circuitBreaker.getState(), memberId);
        } catch (Exception e) {
            log.warn("[Cache] Failed to cache member {}: {}", memberId, e.getMessage());
            throw new RuntimeException("Cache write failed", e);
        }
    }

    public void markAsDirty(Integer roomId) {
        try {
            circuitBreaker.executeRunnable(
                    () -> redisJsonMapper.getStringTemplate().opsForSet().add(DIRTY_ROOMS_SET, String.valueOf(roomId)));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping markAsDirty for room {}.", circuitBreaker.getState(), roomId);
        } catch (Exception e) {
            log.warn("[Cache] Failed to mark room {} as dirty: {}", roomId, e.getMessage());
            throw new RuntimeException("Cache write failed", e);
        }
    }

    public boolean updateReadStatusInCache(Integer roomId, Integer userId, LocalDateTime time) {
        String key = ROOM_KEY + roomId;
        String timeStr = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try {
            return circuitBreaker.executeSupplier(() -> {
                boolean updated = redisJsonMapper.executeUpdateReadStatus(
                        key,
                        userId,
                        timeStr,
                        Duration.ofDays(DEFAULT_TTL_DAYS).toSeconds());

                if (updated) {
                    markAsDirty(roomId);
                    return true;
                }
                return false;
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping read status update for room {}.", circuitBreaker.getState(), roomId);
            return false;
        } catch (Exception e) {
            log.warn("[Cache] Failed to atomic update read status room {}: {}", roomId, e.getMessage());
            // Return false to trigger DB fallback
            return false;
        }
    }

    public void updateMessageMetadataAtomic(Integer roomId, String preview, LocalDateTime time, Integer senderId) {
        String key = ROOM_KEY + roomId;
        // Format matches Jackson's ISO-8601 default
        String timeStr = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try {
            circuitBreaker.executeRunnable(() -> {
                String result = redisJsonMapper.executeUpdateMessageMeta(
                        key,
                        preview,
                        timeStr,
                        senderId,
                        Duration.ofDays(DEFAULT_TTL_DAYS).toSeconds());

                // If result is 'OK', mark as dirty for write-behind
                if (result != null) {
                    markAsDirty(roomId);
                }
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping atomic update for room {}.", circuitBreaker.getState(), roomId);
        } catch (Exception e) {
            log.warn("[Cache] Failed to atomic update room {}: {}", roomId, e.getMessage());
            throw new RuntimeException("Cache write failed", e);
        }
    }

    // =================================================================================
    // LOOKUP & LIST OPERATIONS
    // =================================================================================

    public Optional<Integer> getRoomIdByMembers(Integer memId1, Integer memId2, Integer type) {
        int id1 = Math.min(memId1, memId2);
        int id2 = Math.max(memId1, memId2);
        int safeType = type != null ? type : 0;
        String lookupKey = REDIS_ROOM_LOOKUP_KEY + id1 + ":" + id2 + ":" + safeType;

        try {
            return circuitBreaker.executeSupplier(() -> {
                // Now returns String
                String idStr = redisJsonMapper.getStringTemplate().opsForValue().get(lookupKey);
                if (idStr != null) {
                    return Optional.of(Integer.parseInt(idStr));
                }
                return Optional.empty();
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping room lookup {}.", circuitBreaker.getState(), lookupKey);
            return Optional.empty();
        } catch (Exception e) {
            log.debug("[Cache] Failed to lookup room {}: {}", lookupKey, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Integer> getRoomIdByMembers(Integer memId1, Integer memId2) {
        return getRoomIdByMembers(memId1, memId2, 0);
    }

    public void setRoomIdLookup(Integer memId1, Integer memId2, Integer type, Integer chatroomId) {
        int id1 = Math.min(memId1, memId2);
        int id2 = Math.max(memId1, memId2);
        int safeType = type != null ? type : 0;
        String lookupKey = REDIS_ROOM_LOOKUP_KEY + id1 + ":" + id2 + ":" + safeType;

        try {
            circuitBreaker.executeRunnable(
                    () -> redisJsonMapper.getStringTemplate().opsForValue().set(lookupKey, String.valueOf(chatroomId),
                            Duration.ofDays(DEFAULT_TTL_DAYS)));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache write for room lookup {}.", circuitBreaker.getState(),
                    lookupKey);
        } catch (Exception e) {
            log.warn("[Cache] Failed to cache room lookup {}: {}", lookupKey, e.getMessage());
        }
    }

    public void setRoomIdLookup(Integer memId1, Integer memId2, Integer chatroomId) {
        setRoomIdLookup(memId1, memId2, 0, chatroomId);
    }

    public List<Integer> getUserRoomIds(Integer userId) {
        String key = REDIS_USER_ROOMS_KEY + userId;
        try {
            return circuitBreaker.executeSupplier(() -> {
                List<String> ids = redisJsonMapper.getStringTemplate().opsForList().range(key, 0, -1);
                if (ids == null)
                    return Collections.emptyList();

                return ids.stream()
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping read for user rooms {}.", circuitBreaker.getState(), userId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.debug("[Cache] Failed to read room IDs for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void setUserRoomIds(Integer userId, List<Integer> roomIds) {
        String key = REDIS_USER_ROOMS_KEY + userId;
        try {
            circuitBreaker.executeRunnable(() -> {
                redisJsonMapper.delete(key);
                if (!roomIds.isEmpty()) {
                    List<String> stringIds = roomIds.stream().map(String::valueOf).toList();
                    redisJsonMapper.getStringTemplate().opsForList().rightPushAll(key, stringIds);
                    redisJsonMapper.expire(key, Duration.ofDays(DEFAULT_TTL_DAYS));
                }
            });
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache set for user rooms {}.", circuitBreaker.getState(), userId);
        } catch (Exception e) {
            log.debug("[Cache] Failed to set room IDs for user {}: {}", userId, e.getMessage());
        }
    }

    public void invalidateUserRoomList(Integer userId) {
        String key = REDIS_USER_ROOMS_KEY + userId;
        try {
            circuitBreaker.executeRunnable(() -> redisJsonMapper.delete(key));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Queueing user {} for recovery.", circuitBreaker.getState(), userId);
            pendingRecoveryUserRooms.add(userId);
            // Do not throw - let business logic continue
        } catch (Exception e) {
            log.warn("[Cache] Failed to invalidate user rooms {}: {}", userId, e.getMessage());
            pendingRecoveryUserRooms.add(userId);
            // Do not throw - let business logic continue
        }
    }

    public boolean invalidate(String key) {
        try {
            circuitBreaker.executeRunnable(
                    () -> redisJsonMapper.delete(key));
            return true;
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping invalidation for key {}.", circuitBreaker.getState(), key);
            return false;
        } catch (Exception e) {
            log.warn("[Cache] Failed to invalidate key {}: {}", key, e.getMessage());
            return false;
        }
    }

    public void invalidateRoom(Integer roomId) {
        try {
            circuitBreaker.executeRunnable(() -> redisJsonMapper.delete(ROOM_KEY + roomId));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Queueing room {} for recovery.", circuitBreaker.getState(), roomId);
            pendingRecoveryRooms.add(roomId);
            // Do not throw - let business logic continue
        } catch (Exception e) {
            log.warn("[Cache] Failed to invalidate room {}: {}", roomId, e.getMessage());
            pendingRecoveryRooms.add(roomId);
            // Do not throw - let business logic continue
        }
    }

    // =================================================================================
    // RECOVERY OPERATIONS
    // =================================================================================

    public void queueForRecovery(Integer roomId) {
        this.pendingRecoveryRooms.add(roomId);
    }

    public Set<Integer> getRecoveryQueue() {
        return this.pendingRecoveryRooms;
    }

    public Set<Integer> getUserRoomRecoveryQueue() {
        return this.pendingRecoveryUserRooms;
    }
}
