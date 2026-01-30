package com.petguardian.chat.service.chatroom;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatRoomRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Arrays;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Unified Service for Chat Room and Member Metadata.
 * Consolidates Reader/Writer logic and replaces ResilienceProxies.
 * Uses Resilience4j Functional API for circuit breaking.
 */
@Service
public class ChatRoomMetadataService {

    private static final Logger log = LoggerFactory.getLogger(ChatRoomMetadataService.class);
    private static final String REDIS_ROOM_KEY = "chat:room_meta:";
    private static final String REDIS_MEMBER_KEY = "chat:member_meta:";
    private static final String REDIS_ROOM_LIST_KEY = "chat:user_rooms:";
    private static final String CIRCUIT_NAME = "metadataCircuit";
    private static final long TTL_DAYS = 7;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CircuitBreaker circuitBreaker;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public ChatRoomMetadataService(
            ChatRoomRepository chatRoomRepository,
            ChatMemberRepository memberRepository,
            RedisTemplate<String, Object> redisTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.chatRoomRepository = chatRoomRepository;
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_NAME);
        this.objectMapper = objectMapper;
    }

    // =================================================================================
    // READ OPERATIONS
    // =================================================================================

    public ChatRoomMetadataDTO getRoomMetadata(Integer chatroomId) {
        if (chatroomId == null)
            return null;
        try {
            return circuitBreaker.executeSupplier(() -> fetchRoomMetadata(chatroomId));
        } catch (Exception e) {
            log.warn("[Meta] Circuit Open/Error ({}): fallback to DB for room {}", e.toString(), chatroomId);
            return chatRoomRepository.findById(chatroomId).map(this::convertToDto).orElse(null);
        }
    }

    public MemberProfileDTO getMemberProfile(Integer memberId) {
        if (memberId == null)
            return null;
        try {
            return circuitBreaker.executeSupplier(() -> fetchMemberProfile(memberId));
        } catch (Exception e) {
            log.warn("[Meta] Circuit Open/Error ({}): fallback to DB for member {}", e.toString(), memberId);
            return memberRepository.findById(memberId)
                    .map(m -> new MemberProfileDTO(m.getMemId(), m.getMemName(), null))
                    .orElse(null);
        }
    }

    public Map<Integer, MemberProfileDTO> getMemberProfiles(List<Integer> memberIds) {
        if (memberIds == null || memberIds.isEmpty())
            return Collections.emptyMap();

        Map<Integer, MemberProfileDTO> result = new HashMap<>();
        for (Integer id : memberIds) {
            MemberProfileDTO profile = getMemberProfile(id);
            if (profile != null) {
                result.put(id, profile);
            }
        }
        return result;
    }

    public List<ChatRoomMetadataDTO> getUserChatrooms(Integer userId) {
        if (userId == null)
            return Collections.emptyList();

        // Try Cache first (List of Integers)
        String key = REDIS_ROOM_LIST_KEY + userId;
        try {
            List<Object> roomIds = circuitBreaker.executeSupplier(() -> redisTemplate.opsForList().range(key, 0, -1));
            if (roomIds != null && !roomIds.isEmpty()) {
                List<Integer> ids = roomIds.stream()
                        .map(o -> (Integer) o)
                        .collect(Collectors.toList());
                // Fetch details for each room (which will hit their own caches)
                List<ChatRoomMetadataDTO> results = new ArrayList<>();
                for (Integer rid : ids) {
                    ChatRoomMetadataDTO meta = getRoomMetadata(rid);
                    if (meta != null)
                        results.add(meta);
                }
                return results;
            }
        } catch (Exception e) {
            log.warn("[Meta] Redis List read failed: {}", e.getMessage());
        }

        // Fallback to DB
        List<ChatRoomEntity> entities = chatRoomRepository.findByMemId1OrMemId2(userId, userId);

        List<ChatRoomMetadataDTO> dtos = entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // Cache Refill (Fire and Forget)
        if (!dtos.isEmpty()) {
            try {
                circuitBreaker.executeRunnable(() -> {
                    redisTemplate.delete(key);
                    List<Integer> ids = dtos.stream().map(ChatRoomMetadataDTO::getChatroomId)
                            .collect(Collectors.toList());
                    redisTemplate.opsForList().rightPushAll(key, ids.toArray());
                    redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS);
                });
            } catch (Exception e) {
                log.warn("[Meta] Cache refill failed: {}", e.getMessage());
            }
        }
        return dtos;
    }

    public Optional<ChatRoomMetadataDTO> findRoomByMembers(Integer memId1, Integer memId2) {
        List<ChatRoomEntity> rooms = chatRoomRepository.findByMemId1OrMemId2(memId1, memId1);
        return rooms.stream()
                .filter(r -> (r.getMemId1().equals(memId1) && r.getMemId2().equals(memId2)) ||
                        (r.getMemId1().equals(memId2) && r.getMemId2().equals(memId1)))
                .findFirst()
                .map(this::convertToDto);
    }

    // =================================================================================
    // WRITE OPERATIONS
    // =================================================================================

    @Async
    @Transactional
    public void syncRoomMetadata(Integer chatroomId, String preview, LocalDateTime time, Integer senderId) {
        chatRoomRepository.updateRoomMetadataAtomic(chatroomId, preview, time, senderId);
        // Fire-and-forget invalidation with circuit protection
        invalidateRoomCache(chatroomId);
    }

    @Async
    @Transactional
    public void updateLastReadAt(Integer chatroomId, Integer userId, LocalDateTime time) {
        chatRoomRepository.updateMemberReadStatus(chatroomId, userId, time);
        invalidateRoomCache(chatroomId);
    }

    public void addUserToRoom(Integer userId, Integer chatroomId) {
        try {
            circuitBreaker.executeRunnable(() -> {
                redisTemplate.delete(REDIS_ROOM_LIST_KEY + userId);
            });
        } catch (Exception e) {
            log.warn("[Meta] Failed to invalidate user room list cache: {}", e.getMessage());
        }
    }

    // =================================================================================
    // INTERNAL HELPERS
    // =================================================================================

    private ChatRoomMetadataDTO fetchRoomMetadata(Integer chatroomId) {
        return getFromCacheOrDb(
                REDIS_ROOM_KEY + chatroomId,
                ChatRoomMetadataDTO.class,
                () -> chatRoomRepository.findById(chatroomId).map(this::convertToDto).orElse(null));
    }

    private MemberProfileDTO fetchMemberProfile(Integer memberId) {
        return getFromCacheOrDb(
                REDIS_MEMBER_KEY + memberId,
                MemberProfileDTO.class,
                () -> memberRepository.findById(memberId)
                        .map(m -> new MemberProfileDTO(m.getMemId(), m.getMemName(), null))
                        .orElse(null));
    }

    /**
     * Generic Cache-Aside Helper.
     * Tries Redis -> Fallback to DB -> Writes back to Redis.
     */
    private <T> T getFromCacheOrDb(String key, Class<T> type, java.util.function.Supplier<T> dbLoader) {
        // 1. Try Cache (Propagate DB/Redis Exceptions to Trip Circuit)
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            if (type.isInstance(cached)) {
                return type.cast(cached);
            } else {
                // Fallback: Handle LinkedHashMap from Redis (Jackson default)
                try {
                    return objectMapper.convertValue(cached, type);
                } catch (IllegalArgumentException e) {
                    log.warn("[Meta] Cache Type Mismatch & Conversion Failed. Expected {}, Got {}",
                            type.getSimpleName(), cached.getClass().getSimpleName());
                    // This is a data error, not an infrastructure error.
                    // Treat as cache miss, do not trip circuit.
                }
            }
        }

        // 2. Fallback to DB (Only reached if Cache Miss or Data Error)
        T data = dbLoader.get();

        // 3. Write Back (Only if DB found data)
        if (data != null) {
            try {
                redisTemplate.opsForValue().set(key, data, TTL_DAYS, TimeUnit.DAYS);
            } catch (Exception e) {
                // Determine if we should trip the circuit for Write failures too.
                // For now, logging allows partial degradation (Read DB + Write Skip).
                log.warn("[Meta] Redis write failed for {}: {}", key, e.getMessage());
            }
        }

        return data;
    }

    private void invalidateRoomCache(Integer chatroomId) {
        try {
            circuitBreaker.executeRunnable(() -> {
                redisTemplate.delete(REDIS_ROOM_KEY + chatroomId);
            });
        } catch (Exception e) {
            // Circuit Open (CallNotPermittedException) or Redis Timeout falls here
            // Logs a warning but allows flow to proceed (Fail Safe)
            if (log.isDebugEnabled()) {
                log.debug("[Meta] Invalidation skipped: {}", e.getMessage());
            }
        }
    }

    private ChatRoomMetadataDTO convertToDto(ChatRoomEntity e) {
        ChatRoomMetadataDTO dto = new ChatRoomMetadataDTO();
        dto.setChatroomId(e.getChatroomId());
        dto.setChatroomName(e.getChatroomName() != null ? e.getChatroomName() : "Chat");
        dto.setLastMessagePreview(e.getLastMessagePreview());
        dto.setLastMessageAt(e.getLastMessageAt());
        dto.setMemberIds(Arrays.asList(e.getMemId1(), e.getMemId2()));
        dto.setMem1LastReadAt(e.getMem1LastReadAt());
        dto.setMem2LastReadAt(e.getMem2LastReadAt());
        return dto;
    }
}
