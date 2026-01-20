package com.petguardian.chat.service;

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
        // 1. Fetch all raw chatrooms
        List<ChatRoomEntity> chatrooms = chatroomRepository.findByMemId1OrMemId2(currentUserId, currentUserId);

        // 2. Resolve ALL partner IDs for batch loading (Avoid N+1)
        java.util.Set<Integer> partnerIds = chatrooms.stream()
                .map(r -> r.getOtherMemberId(currentUserId))
                .collect(Collectors.toSet());

        Map<Integer, ChatMemberEntity> memberMap = memberRepository.findAllById(partnerIds)
                .stream()
                .collect(Collectors.toMap(ChatMemberEntity::getMemId,
                        Function.identity()));

        // 3. Map to DTOs
        List<ChatRoomDTO> summaryList = new java.util.ArrayList<>();

        for (ChatRoomEntity room : chatrooms) {
            ChatRoomDTO dto = new ChatRoomDTO();
            dto.setChatroomId(room.getChatroomId());

            Integer partnerId = room.getOtherMemberId(currentUserId);
            dto.setPartnerId(partnerId);

            ChatMemberEntity partner = memberMap.get(partnerId);
            String partnerName = (partner != null) ? partner.getMemName() : "Unknown User";

            // Naming Logic: "PartnerName - RoomLabel"
            String roomLabel;
            if (room.getChatroomName() != null && !room.getChatroomName().isEmpty()) {
                roomLabel = room.getChatroomName();
            } else {
                // Fallback by Type
                int type = room.getChatroomType() != null ? room.getChatroomType() : 0;
                switch (type) {
                    case 0:
                        roomLabel = "寵物服務";
                        break;
                    case 1:
                        roomLabel = "商品諮詢";
                        break;
                    default:
                        roomLabel = "一般聊天";
                }
            }
            dto.setDisplayName(partnerName + " - " + roomLabel);
            dto.setLastMessage(room.getLastMessagePreview());
            dto.setLastMessageTime(room.getLastMessageAt());

            // Unread Logic
            boolean isUnread = false;
            // Only consider unread if there IS a message
            if (room.getLastMessageAt() != null) {
                if (currentUserId.equals(room.getMemId1())) {
                    if (room.getMem1LastReadAt() == null || room.getLastMessageAt().isAfter(room.getMem1LastReadAt())) {
                        isUnread = true;
                    }
                } else if (currentUserId.equals(room.getMemId2())) {
                    if (room.getMem2LastReadAt() == null || room.getLastMessageAt().isAfter(room.getMem2LastReadAt())) {
                        isUnread = true;
                    }
                }
            }
            dto.setUnread(isUnread);

            summaryList.add(dto);
        }

        // 4. Sort by Latest Activity DESC
        summaryList.sort((d1, d2) -> {
            if (d1.getLastMessageTime() == null)
                return 1;
            if (d2.getLastMessageTime() == null)
                return -1;
            return d2.getLastMessageTime().compareTo(d1.getLastMessageTime());
        });

        return summaryList;
    }
}
