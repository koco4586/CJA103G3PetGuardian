package com.petguardian.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for Chat Room Read Receipt Events.
 * Used for broadcasting read status updates via WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatReadReceiptDTO {
    private Integer chatroomId;
    private Integer readerId;
    private LocalDateTime readAt;
}
