package com.petguardian.chat.service.mapper;

import java.util.List;
import java.util.Map;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.dto.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageEntity;

/**
 * Service for mapping Chat Entities to DTOs.
 * Handles complex mapping logic including resolving sender details and reply
 * contexts.
 */
public interface ChatMessageMapper {

        /**
         * Converts a single message entity to DTO with provided context.
         */
        ChatMessageDTO toDto(ChatMessageEntity entity, MemberProfileDTO sender, String replyContent,
                        String replySenderName,
                        Integer currentUserId, Integer partnerId, Integer reportStatus);

        /**
         * Bulk converts a list of message entities to DTOs.
         * Efficiently resolves all dependencies (members, replies) to avoid N+1
         * queries.
         * 
         * @param entities      List of message entities
         * @param currentUserId Current user ID
         * @param partnerId     Partner user ID
         * @param memberMap     Pre-resolved map of members
         * @param replyMap      Pre-resolved map of reply messages
         * @return List of DTOs
         */
        List<ChatMessageDTO> toDtoList(List<ChatMessageEntity> entities, Integer currentUserId, Integer partnerId,
                        Map<Integer, MemberProfileDTO> memberMap,
                        Map<String, ChatMessageEntity> replyMap,
                        Map<String, Integer> reportStatusMap);
}
