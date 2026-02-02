package com.petguardian.chat.service.chatroom;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.redis.RedisJsonMapper;
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
import java.util.stream.Collectors;

/**
 * Domain Cache for ChatRoom Metadata and Member Profiles.
 * Uses RedisJsonMapper for type-safe storage in Hash structures.
 */
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

    private final RedisJsonMapper redisJsonMapper;
    private final CircuitBreaker circuitBreaker;

    public ChatRoomMetadataCache(
            RedisJsonMapper redisJsonMapper,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.redisJsonMapper = redisJsonMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_NAME);
    }

    // =================================================================================
    // READ OPERATIONS
    // =================================================================================

    public Optional<ChatRoomMetadataDTO> getRoomMeta(Integer roomId) {
        try {
            return circuitBreaker.executeSupplier(
                    () -> redisJsonMapper.getHash(ROOM_KEY + roomId, ChatRoomMetadataDTO.class));
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
            return circuitBreaker.executeSupplier(
                    () -> redisJsonMapper.getHash(MEMBER_KEY + memberId, MemberProfileDTO.class));
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

        // Multi-fetching hashes via pipeline or individual calls (simplified for now)
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
            circuitBreaker.executeRunnable(
                    () -> redisJsonMapper.setHash(ROOM_KEY + roomId, dto, Duration.ofDays(DEFAULT_TTL_DAYS)));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache write for room {}.", circuitBreaker.getState(), roomId);
        } catch (Exception e) {
            log.warn("[Cache] Failed to cache room {}: {}", roomId, e.getMessage());
        }
    }

    public void setMemberProfile(Integer memberId, MemberProfileDTO dto) {
        try {
            circuitBreaker.executeRunnable(
                    () -> redisJsonMapper.setHash(MEMBER_KEY + memberId, dto, Duration.ofDays(DEFAULT_TTL_DAYS)));
        } catch (CallNotPermittedException e) {
            log.debug("[Cache] CB is {}. Skipping cache write for member {}.", circuitBreaker.getState(), memberId);
        } catch (Exception e) {
            log.warn("[Cache] Failed to cache member {}: {}", memberId, e.getMessage());
        }
    }

    public void markAsDirty(Integer roomId) {
        try {
            redisJsonMapper.getTemplate().opsForSet().add(DIRTY_ROOMS_SET, roomId);
        } catch (Exception e) {
            log.warn("[Cache] Failed to mark room {} as dirty: {}", roomId, e.getMessage());
        }
    }

    public void updateReadStatusInCache(Integer roomId, Integer userId, LocalDateTime time) {
        getRoomMeta(roomId).ifPresent(dto -> {
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
        });
    }

    // =================================================================================
    // LOOKUP & LIST OPERATIONS
    // =================================================================================

    /**
     * Finds a chatroomId by member IDs using a lookup cache.
     */
    public Optional<Integer> getRoomIdByMembers(Integer memId1, Integer memId2) {
        int id1 = Math.min(memId1, memId2);
        int id2 = Math.max(memId1, memId2);
        String lookupKey = REDIS_ROOM_LOOKUP_KEY + id1 + ":" + id2;

        try {
            Object id = redisJsonMapper.getTemplate().opsForValue().get(lookupKey);
            if (id instanceof Number)
                return Optional.of(((Number) id).intValue());
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void setRoomIdLookup(Integer memId1, Integer memId2, Integer chatroomId) {
        int id1 = Math.min(memId1, memId2);
        int id2 = Math.max(memId1, memId2);
        String lookupKey = REDIS_ROOM_LOOKUP_KEY + id1 + ":" + id2;
        redisJsonMapper.getTemplate().opsForValue().set(lookupKey, chatroomId, Duration.ofDays(DEFAULT_TTL_DAYS));
    }

    public List<Integer> getUserRoomIds(Integer userId) {
        String key = REDIS_USER_ROOMS_KEY + userId;
        try {
            List<Object> ids = redisJsonMapper.getTemplate().opsForList().range(key, 0, -1);
            if (ids == null)
                return Collections.emptyList();
            return ids.stream()
                    .map(o -> o instanceof Number ? ((Number) o).intValue() : Integer.parseInt(o.toString()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.debug("[Cache] Failed to read room IDs for user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void setUserRoomIds(Integer userId, List<Integer> roomIds) {
        String key = REDIS_USER_ROOMS_KEY + userId;
        try {
            redisJsonMapper.delete(key);
            if (!roomIds.isEmpty()) {
                redisJsonMapper.getTemplate().opsForList().rightPushAll(key, roomIds.toArray());
                redisJsonMapper.expire(key, Duration.ofDays(DEFAULT_TTL_DAYS));
            }
        } catch (Exception e) {
            log.debug("[Cache] Failed to set room IDs for user {}: {}", userId, e.getMessage());
        }
    }

    public void invalidateUserRoomList(Integer userId) {
        redisJsonMapper.delete(REDIS_USER_ROOMS_KEY + userId);
    }

    public void invalidate(String key) {
        redisJsonMapper.delete(key);
    }
}
