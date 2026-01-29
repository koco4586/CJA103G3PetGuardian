package com.petguardian.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
    private String replyToId; // TSID reference
    private String replyToContent;
    private String replyToSenderName;
    private Integer chatroomId;
    private Boolean isRead;
    private Integer reportStatus; // 0:None, 1:Pending, 2:Processed, 3:Rejected
    private LocalDateTime chatTime;
}
