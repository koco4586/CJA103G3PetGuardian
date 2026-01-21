package com.petguardian.chat.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberEntity;
import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;

@Service
public class ChatRoomMapperImpl implements ChatRoomMapper {

    private final ChatMemberRepository memberRepository;

    public ChatRoomMapperImpl(ChatMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public ChatRoomDTO toDto(ChatRoomEntity chatRoomEntity, Integer currentUserId) {
        if (chatRoomEntity == null) {
            return null;
        }

        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setChatroomId(chatRoomEntity.getChatroomId());

        Integer partnerId = chatRoomEntity.getOtherMemberId(currentUserId);
        dto.setPartnerId(partnerId);
        dto.setDisplayName(resolveDisplayName(chatRoomEntity, partnerId));

        dto.setLastMessage(chatRoomEntity.getLastMessagePreview());
        dto.setLastMessageTime(chatRoomEntity.getLastMessageAt());
        dto.setUnread(isUnread(chatRoomEntity, currentUserId));

        return dto;
    }

    private String resolveDisplayName(ChatRoomEntity chatRoomEntity, Integer partnerId) {
        String partnerName = memberRepository.findById(partnerId)
                .map(ChatMemberEntity::getMemName)
                .orElse("Unknown User");

        String roomTag = resolveRoomTag(chatRoomEntity);
        return partnerName + " - " + roomTag;
    }

    private String resolveRoomTag(ChatRoomEntity chatRoomEntity) {
        if (chatRoomEntity.getChatroomName() != null && !chatRoomEntity.getChatroomName().isEmpty()) {
            return chatRoomEntity.getChatroomName();
        }

        int type = chatRoomEntity.getChatroomType() != null ? chatRoomEntity.getChatroomType() : 0;
        switch (type) {
            case 0:
                return "寵物服務";
            case 1:
                return "商品諮詢";
            default:
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
