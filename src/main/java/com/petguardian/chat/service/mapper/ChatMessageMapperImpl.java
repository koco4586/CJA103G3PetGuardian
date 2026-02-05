package com.petguardian.chat.service.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.dto.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageEntity;
import io.hypersistence.tsid.TSID;

@Service
public class ChatMessageMapperImpl implements ChatMessageMapper {
    @Override
    public ChatMessageDTO toDto(ChatMessageEntity chatMessageEntity, MemberProfileDTO sender, String replyContent,
            String replySenderName,
            Integer currentUserId, Integer partnerId, Integer reportStatus) {
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setMessageId(TSID.from(chatMessageEntity.getMessageId()).toString());
        chatMessageDTO.setSenderId(chatMessageEntity.getMemberId());
        chatMessageDTO.setReceiverId(chatMessageEntity.getMemberId().equals(currentUserId) ? partnerId : currentUserId);
        chatMessageDTO.setContent(chatMessageEntity.getMessage());
        chatMessageDTO.setSenderName(sender != null ? sender.getMemberName() : "Unknown");
        chatMessageDTO.setChatroomId(chatMessageEntity.getChatroomId());
        chatMessageDTO.setChatTime(chatMessageEntity.getChatTime());
        chatMessageDTO.setReportStatus(reportStatus != null ? reportStatus : 0);

        if (chatMessageEntity.getReplyToMessageId() != null) {
            chatMessageDTO.setReplyToId(TSID.from(chatMessageEntity.getReplyToMessageId()).toString());
            chatMessageDTO.setReplyToContent(replyContent);
            chatMessageDTO.setReplyToSenderName(replySenderName);
        }

        return chatMessageDTO;
    }

    // ============================================================
    // PUBLIC OPERATIONS
    // ============================================================
    /**
     * Bulk converts messages to DTOs.
     */
    @Override
    public List<ChatMessageDTO> toDtoList(List<ChatMessageEntity> chatMessageEntities, Integer currentUserId,
            Integer partnerId,
            Map<Integer, MemberProfileDTO> memberMap,
            Map<Long, ChatMessageEntity> replyMap,
            Map<Long, Integer> reportStatusMap) {
        if (chatMessageEntities.isEmpty()) {
            return Collections.emptyList();
        }

        return chatMessageEntities.stream()
                .map(msg -> mapToDto(msg, memberMap, replyMap, reportStatusMap, currentUserId, partnerId))
                .collect(Collectors.toList());
    }

    // ============================================================
    // INTERNAL HELPERS - MAPPING LOGIC
    // ============================================================

    /**
     * Maps a single entity to DTO using pre-resolved dependency maps.
     */
    private ChatMessageDTO mapToDto(ChatMessageEntity msg,
            Map<Integer, MemberProfileDTO> memberMap,
            Map<Long, ChatMessageEntity> replyMap,
            Map<Long, Integer> reportStatusMap,
            Integer currentUserId,
            Integer partnerId) {
        // Resolve Sender
        MemberProfileDTO sender = memberMap.get(msg.getMemberId());

        // Resolve Reply Context
        String replyContent = null;
        String replySenderName = null;

        if (msg.getReplyToMessageId() != null) {
            ChatMessageEntity replyMsg = replyMap.get(msg.getReplyToMessageId());
            if (replyMsg != null) {
                replyContent = replyMsg.getMessage();
                MemberProfileDTO replySender = memberMap.get(replyMsg.getMemberId());
                replySenderName = (replySender != null) ? replySender.getMemberName() : null;
            }
        }

        // Resolve Report Status
        Integer status = reportStatusMap != null ? reportStatusMap.getOrDefault(msg.getMessageId(), 0) : 0;

        // Delegate to base mapper
        return toDto(msg, sender, replyContent, replySenderName, currentUserId, partnerId, status);
    }
}
