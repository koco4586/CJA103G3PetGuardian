package com.petguardian.chat.service.mapper;

import java.util.List;
import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMemberEntity;

/**
 * Service for mapping Chat Entities to DTOs.
 * Handles complex mapping logic including resolving sender details and reply
 * contexts.
 */
public interface ChatMessageMapper {

    /**
     * Converts a single message entity to DTO with provided context.
     */
    ChatMessageDTO toDto(ChatMessageEntity entity, ChatMemberEntity sender, String replyContent, String replySenderName,
            Integer currentUserId, Integer partnerId);

    /**
     * Bulk converts a list of message entities to DTOs.
     * Efficiently resolves all dependencies (members, replies) to avoid N+1
     * queries.
     */
    List<ChatMessageDTO> toDtoList(List<ChatMessageEntity> entities, Integer currentUserId, Integer partnerId);

    /**
     * enriching a DTO with reply context.
     * Used for single message processing.
     */
    void decorateReplyContext(ChatMessageDTO dto, String replyToMessageId);
}
