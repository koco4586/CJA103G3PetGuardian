package com.petguardian.chat.service.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberEntity;
import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;
import java.util.Map;

@Service
public class ChatRoomMapperImpl implements ChatRoomMapper {

    @Override
    public ChatRoomDTO toDto(ChatRoomEntity chatRoomEntity, Integer currentUserId, String partnerName) {
        if (chatRoomEntity == null) {
            return null;
        }
        return mapBaseFields(chatRoomEntity, currentUserId, partnerName);
    }

    @Override
    public ChatRoomDTO toDto(ChatRoomEntity chatRoomEntity, Integer currentUserId,
            Map<Integer, ChatMemberEntity> preloadedMembers) {
        if (chatRoomEntity == null) {
            return null;
        }

        Integer partnerId = chatRoomEntity.getOtherMemberId(currentUserId);
        ChatMemberEntity partner = preloadedMembers.get(partnerId);
        String partnerName = (partner != null) ? partner.getMemName() : "Unknown User";

        return mapBaseFields(chatRoomEntity, currentUserId, partnerName);
    }

    /**
     * Shared mapping logic for base fields.
     */
    private ChatRoomDTO mapBaseFields(ChatRoomEntity chatRoomEntity, Integer currentUserId, String partnerName) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setChatroomId(chatRoomEntity.getChatroomId());

        Integer partnerId = chatRoomEntity.getOtherMemberId(currentUserId);
        dto.setPartnerId(partnerId);

        String roomTag = resolveRoomTag(chatRoomEntity);
        dto.setDisplayName(partnerName + " - " + roomTag);

        dto.setLastMessage(chatRoomEntity.getLastMessagePreview());
        dto.setLastMessageTime(chatRoomEntity.getLastMessageAt());
        dto.setUnread(isUnread(chatRoomEntity, currentUserId));

        return dto;
    }

    private String resolveRoomTag(ChatRoomEntity chatRoomEntity) {
        Byte typeByte = chatRoomEntity.getChatroomType();
        int type = typeByte != null ? typeByte.intValue() : 0;

        switch (type) {
            case 0:
                return "寵物服務";
            case 1:
                return "寵物商品諮詢";
            default:
                if (chatRoomEntity.getChatroomName() != null && !chatRoomEntity.getChatroomName().isEmpty()) {
                    return chatRoomEntity.getChatroomName();
                }
                return "一般聊天";
        }
    }

    private boolean isUnread(ChatRoomEntity chatRoomEntity, Integer currentUserId) {
        if (chatRoomEntity.getLastMessageAt() == null) {
            return false;
        }

        LocalDateTime myLastReadAt;
        if (currentUserId.equals(chatRoomEntity.getMemId1())) {
            myLastReadAt = chatRoomEntity.getMem1LastReadAt();
        } else {
            myLastReadAt = chatRoomEntity.getMem2LastReadAt();
        }

        return myLastReadAt == null || chatRoomEntity.getLastMessageAt().isAfter(myLastReadAt);
    }
}
