package com.petguardian.chat.service.chatmessage;

/**
 * Context for creating a new chat message.
 * Encapsulates all necessary data for persistence and processing.
 */
public record MessageCreationContext(
                String messageId,
                Integer chatroomId,
                Integer senderId,
                String content,
                String replyToId) {
}
