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

    private Integer messageId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private String senderName;

    // Reply support
    private Integer replyToId;
    private String replyToContent;
    private String replyToSenderName;
}
