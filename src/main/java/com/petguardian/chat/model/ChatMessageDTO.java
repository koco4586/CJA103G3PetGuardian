package com.petguardian.chat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for WebSocket chat message payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private String messageId; // TSID (13-char string)
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private String senderName;

    // Reply support
    private String replyToId; // TSID reference
    private String replyToContent;
    private String replyToSenderName;

    // Context
    private Integer chatroomId;
}
