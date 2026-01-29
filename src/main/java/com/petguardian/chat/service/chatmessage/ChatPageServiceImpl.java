package com.petguardian.chat.service.chatmessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.petguardian.chat.dto.ChatMemberDTO;
import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataService;
import com.petguardian.chat.service.mapper.ChatRoomMapper;

/**
 * Service Implementation for Chat View Data Aggregation.
 * 
 * Responsibilities:
 * - Optimized data loading for initial page rendering
 * - DTO projection to decouple View layer from JPA Entities
 * - Efficient multi-entity aggregation (e.g. Member + LastMessage)
 */
@Service
public class ChatPageServiceImpl implements ChatPageService {

    // ============================================================
    // DEPENDENCIES
    // ============================================================

    private final ChatRoomMetadataService metadataService;
    private final ChatRoomMapper chatRoomMapper;

    public ChatPageServiceImpl(ChatRoomMetadataService metadataService,
            ChatRoomMapper chatRoomMapper) {
        this.metadataService = metadataService;
        this.chatRoomMapper = chatRoomMapper;
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    @Override
    public ChatMemberDTO getMember(Integer memId) {
        MemberProfileDTO profile = metadataService.getMemberProfile(memId);
        if (profile == null)
            return null;

        ChatMemberDTO dto = new ChatMemberDTO();
        dto.setMemId(profile.getMemberId());
        dto.setMemName(profile.getMemberName());
        return dto;
    }

    /**
     * Retrieves all chatrooms for the current user.
     * <p>
     * Uses batch loading for partner data to avoid N+1 queries.
     * </p>
     * 
     * @param currentUserId The ID of the current user
     * @return List of sorted ChatRoomDTOs
     */
    @Override
    public List<ChatRoomDTO> getMyChatrooms(Integer currentUserId) {
        // 1. Fetch all chatrooms for user via metadata service (Cache First)
        List<ChatRoomMetadataDTO> metadataList = metadataService.getUserChatrooms(currentUserId);
        if (metadataList == null || metadataList.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 2. Batch load partners from cache
        java.util.Set<Integer> partnerIds = metadataList.stream()
                .flatMap(m -> m.getMemberIds().stream())
                .filter(id -> !id.equals(currentUserId))
                .collect(Collectors.toSet());

        Map<Integer, MemberProfileDTO> memberMap = metadataService
                .getMemberProfiles(new java.util.ArrayList<>(partnerIds));

        // 3. Map to DTOs and sort by latest activity
        return metadataList.stream()
                .map(meta -> chatRoomMapper.toDtoFromMeta(meta, currentUserId, memberMap))
                .sorted((d1, d2) -> {
                    if (d1.getLastMessageTime() == null)
                        return 1;
                    if (d2.getLastMessageTime() == null)
                        return -1;
                    return d2.getLastMessageTime().compareTo(d1.getLastMessageTime());
                })
                .collect(Collectors.toList());
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    // No longer needed: batchLoadPartners relies on direct repository access.
    // Unified metadata service handles this now.
}
