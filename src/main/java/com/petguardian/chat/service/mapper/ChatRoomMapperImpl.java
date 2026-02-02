package com.petguardian.chat.service.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberEntity;
import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import java.util.Map;
import java.util.Arrays;

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
        if (chatRoomEntity == null)
            return null;

        Integer partnerId = chatRoomEntity.getOtherMemberId(currentUserId);
        ChatMemberEntity partner = preloadedMembers.get(partnerId);
        String partnerName = (partner != null) ? partner.getMemName() : "Unknown User";

        return mapBaseFields(chatRoomEntity, currentUserId, partnerName);
    }

    @Override
    public ChatRoomDTO toDtoFromMeta(ChatRoomMetadataDTO meta, Integer currentUserId,
            Map<Integer, MemberProfileDTO> preloadedProfiles) {
        if (meta == null)
            return null;

        Integer partnerId = meta.getMemberIds().get(0).equals(currentUserId)
                ? meta.getMemberIds().get(1)
                : meta.getMemberIds().get(0);

        MemberProfileDTO partner = preloadedProfiles.get(partnerId);
        String partnerName = (partner != null) ? partner.getMemberName() : "Unknown User";

        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setChatroomId(meta.getChatroomId());
        dto.setPartnerId(partnerId);
        dto.setDisplayName(partnerName + " - " + resolveRoomTag(meta.getChatroomType(), meta.getChatroomName()));
        dto.setLastMessage(meta.getLastMessagePreview());
        dto.setLastMessageTime(meta.getLastMessageAt());

        LocalDateTime myLastReadAt = meta.getMemberIds().get(0).equals(currentUserId)
                ? meta.getMem1LastReadAt()
                : meta.getMem2LastReadAt();

        dto.setUnread(isUnread(meta.getLastMessageAt(), myLastReadAt));

        return dto;
    }

    private String resolveRoomTag(Byte typeByte, String chatroomName) {
        int type = typeByte != null ? typeByte.intValue() : 0;
        switch (type) {
            case 0:
                return "寵物服務";
            case 1:
                return "寵物商品諮詢";
            default:
                return (chatroomName != null && !chatroomName.isEmpty()) ? chatroomName : "一般聊天";
        }
    }

    private boolean isUnread(LocalDateTime lastMsgAt, LocalDateTime myLastReadAt) {
        if (lastMsgAt == null)
            return false;
        return myLastReadAt == null || lastMsgAt.isAfter(myLastReadAt);
    }

    /**
     * Shared mapping logic for base fields.
     */
    private ChatRoomDTO mapBaseFields(ChatRoomEntity chatRoomEntity, Integer currentUserId, String partnerName) {
        ChatRoomDTO dto = new ChatRoomDTO();
        dto.setChatroomId(chatRoomEntity.getChatroomId());

        Integer partnerId = chatRoomEntity.getOtherMemberId(currentUserId);
        dto.setPartnerId(partnerId);

        dto.setDisplayName(partnerName + " - "
                + resolveRoomTag(chatRoomEntity.getChatroomType(), chatRoomEntity.getChatroomName()));

        dto.setLastMessage(chatRoomEntity.getLastMessagePreview());
        dto.setLastMessageTime(chatRoomEntity.getLastMessageAt());

        LocalDateTime myLastReadAt = currentUserId.equals(chatRoomEntity.getMemId1())
                ? chatRoomEntity.getMem1LastReadAt()
                : chatRoomEntity.getMem2LastReadAt();
        dto.setUnread(isUnread(chatRoomEntity.getLastMessageAt(), myLastReadAt));

        return dto;
    }

    // =========================================================================
    // CACHE LAYER CONVERSION
    // =========================================================================

    /**
     * {@inheritDoc}
     * Converts a ChatRoomEntity to ChatRoomMetadataDTO for Redis caching.
     * This centralizes the mapping logic previously in ChatRoomMetadataService.
     */
    @Override
    public ChatRoomMetadataDTO toMetadataDto(ChatRoomEntity entity) {
        if (entity == null) {
            return null;
        }

        return ChatRoomMetadataDTO.builder()
                .chatroomId(entity.getChatroomId())
                .chatroomName(entity.getChatroomName() != null ? entity.getChatroomName() : "Chat")
                .chatroomType(entity.getChatroomType())
                .chatroomStatus(entity.getChatroomStatus())
                .memberIds(Arrays.asList(entity.getMemId1(), entity.getMemId2()))
                .lastMessagePreview(entity.getLastMessagePreview())
                .lastMessageAt(entity.getLastMessageAt())
                .mem1LastReadAt(entity.getMem1LastReadAt())
                .mem2LastReadAt(entity.getMem2LastReadAt())
                .build();
    }

    @Override
    public MemberProfileDTO toMemberProfileDto(ChatMemberEntity entity) {
        if (entity == null) {
            return null;
        }
        return MemberProfileDTO.builder()
                .memberId(entity.getMemId())
                .memberName(entity.getMemName())
                .memberImage(null) // Base member entity doesn't have image, but we preserve the field
                .build();
    }
}
