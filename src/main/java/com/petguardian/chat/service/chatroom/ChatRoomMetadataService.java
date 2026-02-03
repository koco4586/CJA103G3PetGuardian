package com.petguardian.chat.service.chatroom;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.service.mapper.ChatRoomMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Service for Chat Room and Member Metadata (Domain Layer).
 * Responsibilities:
 * - ChatRoom metadata operations (read/write)
 * - Member profile operations (read/write)
 * - Business logic delegation to ChatRoomMapper and RedisCacheHelper
 * Delegates to:
 * - {@link RedisCacheHelper} - Infrastructure layer (Redis operations)
 * - {@link ChatRoomMapper} - Transformation layer (Entity ↔ DTO)
 */
@Slf4j
@Service
public class ChatRoomMetadataService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository memberRepository;
    private final ChatRoomMetadataCache metadataCache;
    private final ChatRoomMapper mapper;

    public ChatRoomMetadataService(
            ChatRoomRepository chatRoomRepository,
            ChatMemberRepository memberRepository,
            ChatRoomMetadataCache metadataCache,
            ChatRoomMapper mapper) {
        this.chatRoomRepository = chatRoomRepository;
        this.memberRepository = memberRepository;
        this.metadataCache = metadataCache;
        this.mapper = mapper;
    }

    // =================================================================================
    // READ OPERATIONS
    // =================================================================================

    /**
     * Gets room metadata with Hash-first caching strategy.
     */
    public ChatRoomMetadataDTO getRoomMetadata(Integer chatroomId) {
        if (chatroomId == null)
            return null;

        return metadataCache.getRoomMeta(chatroomId)
                .orElseGet(() -> {
                    ChatRoomMetadataDTO dbDto = chatRoomRepository.findById(chatroomId)
                            .map(mapper::toMetadataDto)
                            .orElse(null);
                    if (dbDto != null) {
                        metadataCache.setRoomMeta(chatroomId, dbDto);
                    }
                    return dbDto;
                });
    }

    public MemberProfileDTO getMemberProfile(Integer memberId) {
        if (memberId == null)
            return null;

        return metadataCache.getMemberProfile(memberId)
                .orElseGet(() -> {
                    MemberProfileDTO dbDto = memberRepository.findById(memberId)
                            .map(mapper::toMemberProfileDto)
                            .orElse(null);
                    if (dbDto != null) {
                        metadataCache.setMemberProfile(memberId, dbDto);
                    }
                    return dbDto;
                });
    }

    public Map<Integer, MemberProfileDTO> getMemberProfiles(List<Integer> memberIds) {
        if (memberIds == null || memberIds.isEmpty())
            return Collections.emptyMap();

        // 1. Try Cache
        Map<Integer, MemberProfileDTO> resultMap = new java.util.HashMap<>();
        List<Integer> missingIds = new java.util.ArrayList<>();

        for (Integer id : memberIds) {
            metadataCache.getMemberProfile(id).ifPresentOrElse(
                    dto -> resultMap.put(id, dto),
                    () -> missingIds.add(id));
        }

        // 2. DB Fallback for misses
        if (!missingIds.isEmpty()) {
            memberRepository.findAllByMemIdIn(missingIds).forEach(entity -> {
                MemberProfileDTO dto = mapper.toMemberProfileDto(entity);
                metadataCache.setMemberProfile(dto.getMemberId(), dto);
                resultMap.put(dto.getMemberId(), dto);
            });
        }

        return resultMap;
    }

    public List<ChatRoomMetadataDTO> getUserChatrooms(Integer userId) {
        if (userId == null)
            return Collections.emptyList();

        // 1. Try Cache List
        List<Integer> roomIds = metadataCache.getUserRoomIds(userId);
        if (!roomIds.isEmpty()) {
            List<ChatRoomMetadataDTO> cached = metadataCache.getRoomMetaBatch(roomIds);
            // CRITICAL: If cache returned data, use it. Otherwise fallback to DB.
            // This handles the case where Redis goes down after we got room IDs.
            if (!cached.isEmpty()) {
                return cached;
            }
            // Cache failed to return data - fall through to DB
        }

        // 2. Fallback to DB
        List<ChatRoomEntity> entities = chatRoomRepository.findByMemId1OrMemId2(userId, userId);
        List<ChatRoomMetadataDTO> dtos = entities.stream()
                .map(mapper::toMetadataDto)
                .collect(Collectors.toList());

        // 3. Re-fill cache (best effort)
        if (!dtos.isEmpty()) {
            metadataCache.setUserRoomIds(userId,
                    dtos.stream().map(ChatRoomMetadataDTO::getChatroomId).collect(Collectors.toList()));
            dtos.forEach(dto -> metadataCache.setRoomMeta(dto.getChatroomId(), dto));
        }

        return dtos;
    }

    public Optional<ChatRoomMetadataDTO> findRoomByMembersAndType(Integer memId1, Integer memId2, Integer type) {
        if (memId1 == null || memId2 == null)
            return Optional.empty();

        int safeType = type != null ? type : 0;

        // 1. Check Lookup Cache
        return metadataCache.getRoomIdByMembers(memId1, memId2, safeType)
                .map(this::getRoomMetadata)
                .or(() -> {
                    // 2. Fallback to DB
                    int id1 = Math.min(memId1, memId2);
                    int id2 = Math.max(memId1, memId2);

                    return chatRoomRepository.findByMemId1AndMemId2AndChatroomType(id1, id2, safeType)
                            .map(entity -> {
                                ChatRoomMetadataDTO dto = mapper.toMetadataDto(entity);
                                metadataCache.setRoomIdLookup(memId1, memId2, safeType, dto.getChatroomId()); // Type-Aware
                                                                                                              // Cache
                                                                                                              // Set
                                metadataCache.setRoomMeta(dto.getChatroomId(), dto);
                                return dto;
                            });
                })
                .map(Optional::ofNullable) // Flatten possible null from getRoomMetadata
                .orElse(Optional.empty());
    }

    // Deprecated legacy method
    public Optional<ChatRoomMetadataDTO> findRoomByMembers(Integer memId1, Integer memId2) {
        return findRoomByMembersAndType(memId1, memId2, 0);
    }

    public void cacheRoomLookup(Integer memId1, Integer memId2, Integer type, Integer chatroomId) {
        int safeType = type != null ? type : 0;
        metadataCache.setRoomIdLookup(memId1, memId2, safeType, chatroomId);
    }

    // Deprecated legacy method
    public void cacheRoomLookup(Integer memId1, Integer memId2, Integer chatroomId) {
        cacheRoomLookup(memId1, memId2, 0, chatroomId);
    }

    // =================================================================================
    // WRITE OPERATIONS
    // =================================================================================

    public void syncRoomMetadata(Integer chatroomId, String preview, LocalDateTime time, Integer senderId) {
        metadataCache.getRoomMeta(chatroomId).ifPresent(dto -> {
            dto.setLastMessagePreview(preview);
            dto.setLastMessageAt(time);

            // Sync sender's read status
            List<Integer> members = dto.getMemberIds();
            if (members != null && !members.isEmpty()) {
                if (senderId.equals(members.get(0))) {
                    dto.setMem1LastReadAt(time);
                } else if (members.size() > 1 && senderId.equals(members.get(1))) {
                    dto.setMem2LastReadAt(time);
                }
            }

            metadataCache.setRoomMeta(chatroomId, dto);
            metadataCache.markAsDirty(chatroomId);
        });
    }

    public void updateLastReadAt(Integer chatroomId, Integer userId, LocalDateTime time) {
        boolean cacheUpdated = metadataCache.updateReadStatusInCache(chatroomId, userId, time);

        // CRITICAL: If cache update failed (Redis down), directly update MySQL
        // This ensures read status is persisted even without Redis
        if (!cacheUpdated) {
            log.debug("[MetadataService] Redis unavailable, updating read status directly in MySQL for room {}",
                    chatroomId);
            chatRoomRepository.updateMemberReadStatus(chatroomId, userId, time);
        }
    }

    public void addUserToRoom(Integer userId, Integer chatroomId) {
        metadataCache.invalidateUserRoomList(userId);
    }
}
