package com.petguardian.chat.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Chat Room Metadata.
 * Used for room-level caching and verification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomMetadataDTO implements Serializable {
    private Integer chatroomId;
    private String chatroomName;
    private List<Integer> memberIds;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private Byte chatroomType;
    private Byte chatroomStatus;
    private LocalDateTime mem1LastReadAt;
    private LocalDateTime mem2LastReadAt;
}
