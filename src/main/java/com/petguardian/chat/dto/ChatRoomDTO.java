package com.petguardian.chat.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Data Transfer Object for Chat Sidebar Items.
 * Represents a single chatroom summary entry.
 */
@Data
public class ChatRoomDTO {
    private Integer chatroomId;
    private Integer partnerId;
    private String displayName; // Formatted Name: "Partner - Type"
    private String partnerAvatar; // URL or Placeholder
    private String lastMessage; // Truncated preview
    private LocalDateTime lastMessageTime;
    private boolean unread;
}
