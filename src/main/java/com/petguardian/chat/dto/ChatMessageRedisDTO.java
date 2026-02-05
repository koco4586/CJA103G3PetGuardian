package com.petguardian.chat.dto;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.service.context.MessageCreationContext;
import io.hypersistence.tsid.TSID;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Dedicated DTO for Redis Persistence.
 * Bridges the gap between Long TSIDs (Java/DB) and String TSIDs (Redis/JSON).
 * Ensures Redis is not affected by the BigInt migration and maintains frontend
 * compatibility.
 */
public record ChatMessageRedisDTO(
        String messageId,
        Integer chatroomId,
        Integer senderId,
        String content,
        String replyToId,
        LocalDateTime chatTime,
        Integer messageType) implements Serializable {

    /**
     * Factory from MessageCreationContext.
     * Converts Long IDs -> String for Redis.
     */
    public static ChatMessageRedisDTO fromContext(MessageCreationContext ctx) {
        return new ChatMessageRedisDTO(
                TSID.from(ctx.messageId()).toString(),
                ctx.chatroomId(),
                ctx.senderId(),
                ctx.content(),
                ctx.replyToId() != null ? TSID.from(ctx.replyToId()).toString() : null,
                ctx.createdAt(),
                0 // Default message type
        );
    }

    /**
     * Factory from Entity.
     * Converts Long IDs -> String for Redis (Cache Warmup).
     */
    public static ChatMessageRedisDTO fromEntity(ChatMessageEntity entity) {
        return new ChatMessageRedisDTO(
                TSID.from(entity.getMessageId()).toString(),
                entity.getChatroomId(),
                entity.getMemberId(),
                entity.getMessage(),
                entity.getReplyToMessageId() != null ? TSID.from(entity.getReplyToMessageId()).toString() : null,
                entity.getChatTime(),
                0);
    }

    /**
     * Converts back to Entity for DB Persistence.
     * Converts String IDs -> Long.
     */
    public ChatMessageEntity toEntity() {
        ChatMessageEntity e = new ChatMessageEntity();
        e.setMessageId(TSID.from(this.messageId).toLong());
        e.setChatroomId(this.chatroomId);
        e.setMemberId(this.senderId);
        e.setMessage(this.content);
        e.setChatTime(this.chatTime);
        if (this.replyToId != null) {
            e.setReplyToMessageId(TSID.from(this.replyToId).toLong());
        }
        return e;
    }
}
