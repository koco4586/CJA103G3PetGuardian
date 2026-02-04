package com.petguardian.chat.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Index;

/**
 * Entity: Chat Message.
 * 
 * Represents an individual message in a chat conversation.
 * 
 * Key Design Decisions:
 * - Uses String (CHAR(13)) for IDs to store TSIDs (Time-Sorted Unique
 * Identifiers)
 */
@NamedNativeQuery(name = "ChatMessageEntity.searchByMessage", query = "SELECT * FROM chat_message WHERE chatroom_id = :chatroomId AND MATCH(message) AGAINST(:keyword IN BOOLEAN MODE) ORDER BY chat_time DESC", resultClass = ChatMessageEntity.class)
@Entity
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_chatroom_tsid", columnList = "chatroom_id, message_id DESC"),
        @Index(name = "idx_member_id", columnList = "member_id")
})
@Data
@NoArgsConstructor
public class ChatMessageEntity implements Persistable<String>, Serializable {

    @Id
    @Column(name = "message_id", length = 13, updatable = false)
    private String messageId; // Strategy: TSID (Application Generated)

    @Column(name = "chatroom_id")
    private Integer chatroomId;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "chat_time")
    private LocalDateTime chatTime;

    @Column(name = "reply_to_message_id", length = 13)
    private String replyToMessageId; // Reference to parent message ID

    @Override
    public String getId() {
        return messageId;
    }

    @Override
    public boolean isNew() {
        // Since we use TSID (manually assigned), returning true bypasses the
        // SELECT-before-INSERT check.
        // This is safe because TSID collision is extremely unlikely.
        return true;
    }

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
        // TSID
        return messageId != null ? messageId.hashCode() : getClass().hashCode();
    }

}
