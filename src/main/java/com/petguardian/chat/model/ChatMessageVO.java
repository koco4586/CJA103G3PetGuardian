package com.petguardian.chat.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_message")
@Data
@NoArgsConstructor
public class ChatMessageVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", updatable = false)
    private Integer messageId;

    @Column(name = "chatroom_id")
    private Integer chatroomId;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "message", length = 2000)
    private String message;

    @CreationTimestamp
    @Column(name = "chat_time", insertable = false, updatable = false)
    private LocalDateTime chatTime;

    @Column(name = "reply_to_message_id")
    private Integer replyToMessageId;
}
