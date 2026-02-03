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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChatRoomMetadataCache {

    private static final String ROOM_KEY = "chat:room_meta:";
    private static final String MEMBER_KEY = "chat:member_meta:";
    private static final String REDIS_ROOM_LOOKUP_KEY = "chat:room_lookup:";
    private static final String REDIS_USER_ROOMS_KEY = "chat:user_rooms:";
    private static final String DIRTY_ROOMS_SET = "chat:dirty_rooms";
    private static final String CIRCUIT_NAME = "redisCacheCircuit";
    private static final long DEFAULT_TTL_DAYS = 7;

    // Track rooms that need cache invalidation after Redis recovery
    private final Set<Integer> pendingRecoveryRooms = ConcurrentHashMap.newKeySet();

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

        return ids.stream()
                .map(this::getRoomMeta)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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
        }
    }

    public boolean updateReadStatusInCache(Integer roomId, Integer userId, LocalDateTime time) {
        Optional<ChatRoomMetadataDTO> meta = getRoomMeta(roomId);
        if (meta.isEmpty()) {
            return false;
        }

        ChatRoomMetadataDTO dto = meta.get();
        List<Integer> members = dto.getMemberIds();
        if (members != null && !members.isEmpty()) {
            if (userId.equals(members.get(0))) {
                dto.setMem1LastReadAt(time);
            } else if (members.size() > 1 && userId.equals(members.get(1))) {
                dto.setMem2LastReadAt(time);
            }
            setRoomMeta(roomId, dto);
            markAsDirty(roomId);
        }
        return true;
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
            // Now returns String
            String idStr = redisJsonMapper.getStringTemplate().opsForValue().get(lookupKey);
            if (idStr != null) {
                return Optional.of(Integer.parseInt(idStr));
            }
            return Optional.empty();
        } catch (Exception e) {
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
            List<String> ids = redisJsonMapper.getStringTemplate().opsForList().range(key, 0, -1);
            if (ids == null)
                return Collections.emptyList();

            return ids.stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
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
        invalidate(REDIS_USER_ROOMS_KEY + userId);
    }

    public void invalidate(String key) {
        try {
            circuitBreaker.executeRunnable(
                    () -> redisJsonMapper.delete(key));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping invalidation for key {}.", circuitBreaker.getState(), key);
        } catch (Exception e) {
            log.warn("[Cache] Failed to invalidate key {}: {}", key, e.getMessage());
        }
    }

    public void invalidateRoom(Integer roomId) {
        redisJsonMapper.delete(ROOM_KEY + roomId);
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
}
