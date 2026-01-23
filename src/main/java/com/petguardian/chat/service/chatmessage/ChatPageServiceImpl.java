package com.petguardian.chat.service.chatmessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberDTO;
import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatMemberEntity;

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
    private final ChatMemberRepository memberRepository;
    private final ChatRoomRepository chatroomRepository;

    public ChatPageServiceImpl(ChatMemberRepository memberRepository,
            ChatRoomRepository chatroomRepository) {
        this.memberRepository = memberRepository;
        this.chatroomRepository = chatroomRepository;
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    @Override
    public ChatMemberDTO getMember(Integer memId) {
        return memberRepository.findById(memId)
                .map(ChatMemberDTO::fromEntity)
                .orElse(null);
    }

    @Override
    public List<ChatRoomDTO> getMyChatrooms(Integer currentUserId) {
        // 1. Fetch all chatrooms for user
        List<ChatRoomEntity> chatrooms = chatroomRepository.findByMemId1OrMemId2(currentUserId, currentUserId);

        // 2. Batch load partners (Avoid N+1)
        Map<Integer, ChatMemberEntity> memberMap = batchLoadPartners(chatrooms, currentUserId);

        // 3. Map to DTOs and sort by latest activity
        return chatrooms.stream()
                .map(room -> buildChatroomDto(room, currentUserId, memberMap))
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

    private Map<Integer, ChatMemberEntity> batchLoadPartners(List<ChatRoomEntity> chatrooms, Integer currentUserId) {
        java.util.Set<Integer> partnerIds = chatrooms.stream()
                .map(r -> r.getOtherMemberId(currentUserId))
                .collect(Collectors.toSet());

        return memberRepository.findAllById(partnerIds).stream()
                .collect(Collectors.toMap(ChatMemberEntity::getMemId, Function.identity()));
    }

    private ChatRoomDTO buildChatroomDto(ChatRoomEntity room, Integer currentUserId,
            Map<Integer, ChatMemberEntity> memberMap) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setChatroomId(room.getChatroomId());

        Integer partnerId = room.getOtherMemberId(currentUserId);
        dto.setPartnerId(partnerId);
        dto.setDisplayName(resolveDisplayName(room, partnerId, memberMap));

        dto.setLastMessage(room.getLastMessagePreview());
        dto.setLastMessageTime(room.getLastMessageAt());
        dto.setUnread(isUnread(room, currentUserId));
        return dto;
    }

    private String resolveDisplayName(ChatRoomEntity room, Integer partnerId,
            Map<Integer, ChatMemberEntity> memberMap) {
        ChatMemberEntity partner = memberMap.get(partnerId);
        String partnerName = (partner != null) ? partner.getMemName() : "Unknown User";

        String roomTag = resolveRoomTag(room);
        return partnerName + " - " + roomTag;
    }

    /**
     * Resolves the room label based on name or type.
     */
    private String resolveRoomTag(ChatRoomEntity room) {
        if (room.getChatroomName() != null && !room.getChatroomName().isEmpty()) {
            return room.getChatroomName();
        }

        // Fallback by Type
        int type = room.getChatroomType() != null ? room.getChatroomType() : 0;
        switch (type) {
            case 0:
                return "寵物服務";
            case 1:
                return "商品諮詢";
            default:
                return "一般聊天";
        }
    }

    private boolean isUnread(ChatRoomEntity room, Integer currentUserId) {
        if (room.getLastMessageAt() == null) {
            return false;
        }

        java.time.LocalDateTime myLastReadAt;
        if (currentUserId.equals(room.getMemId1())) {
            myLastReadAt = room.getMem1LastReadAt();
        } else {
            myLastReadAt = room.getMem2LastReadAt();
        }

        return myLastReadAt == null || room.getLastMessageAt().isAfter(myLastReadAt);
    }
}
