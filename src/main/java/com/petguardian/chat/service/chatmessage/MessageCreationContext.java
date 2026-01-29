package com.petguardian.chat.service.chatmessage;

/**
 * Context for creating a new chat message.
 * Encapsulates all necessary data for persistence and processing.
 */

import java.time.LocalDateTime;
import java.time.ZoneId;
import io.hypersistence.tsid.TSID;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.petguardian.chat.model.ChatMessageEntity;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record MessageCreationContext(
        String messageId,
        Integer chatroomId,
        Integer senderId,
        String content,
        String replyToId) {

    public LocalDateTime createdAt() {
        return LocalDateTime.ofInstant(TSID.from(messageId).getInstant(), ZoneId.systemDefault());
    }

    public ChatMessageEntity toEntity() {
        ChatMessageEntity message = new ChatMessageEntity();
        message.setMessageId(this.messageId());
        message.setChatroomId(this.chatroomId());
        message.setMemberId(this.senderId());
        message.setMessage(this.content());
        message.setChatTime(this.createdAt());
        if (this.replyToId() != null) {
            message.setReplyToMessageId(this.replyToId());
        }
        return message;
    }
}
