package com.petguardian.chat.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity: Chat Message.
 * 
 * Represents an individual message in a chat conversation.
 * 
 * Key Design Decisions:
 * - Uses String (CHAR(13)) for IDs to store TSIDs (Time-Sorted Unique
 * Identifiers)
 * - Decoupled from ChatRoom/Member via ID references (No JPA Relations) to
 * allow
 * independent scaling or microservice extraction.
 */
@Entity
@Table(name = "chat_message")
@Data
@NoArgsConstructor
public class ChatMessageEntity implements Serializable {

    @Id
    @Column(name = "message_id", length = 13, updatable = false)
    private String messageId; // Strategy: TSID (Application Generated)

    @Column(name = "chatroom_id")
    private Integer chatroomId;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "message", length = 2000)
    private String message;

    @CreationTimestamp
    @Column(name = "chat_time", insertable = false, updatable = false)
    private LocalDateTime chatTime;

    @Column(name = "reply_to_message_id", length = 13)
    private String replyToMessageId; // Reference to parent message ID

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof ChatMessageEntity))
            return false;
        ChatMessageEntity that = (ChatMessageEntity) object;
        return messageId != null && messageId.equals(that.messageId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
