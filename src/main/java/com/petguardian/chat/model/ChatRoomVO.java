package com.petguardian.chat.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity: Chat Room.
 * 
 * Represents a conversation container between two users.
 * 
 * Optimization Note:
 * Includes denormalized fields (`lastMessageAt`, `lastMessagePreview`) to
 * support
 * efficient high-volume queries for the sidebar "Inbox" view without joining
 * the massive `chat_message` table.
 */
@Entity
@Table(name = "chatroom")
@Data
@NoArgsConstructor
public class ChatRoomVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatroom_id", updatable = false)
    private Integer chatroomId;

    @Column(name = "mem_id1")
    private Integer memId1;

    @Column(name = "mem_id2")
    private Integer memId2;

    @Column(name = "chatroom_name", length = 50)
    private String chatroomName;

    @Column(name = "chatroom_type")
    private Byte chatroomType;

    @Column(name = "chatroom_status", insertable = false)
    private Byte chatroomStatus;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Denormalized for Performance (Inbox Sorting)
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    // Denormalized for Performance (Inbox Preview)
    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;

    @Column(name = "mem1_last_read_at")
    private LocalDateTime mem1LastReadAt;

    @Column(name = "mem2_last_read_at")
    private LocalDateTime mem2LastReadAt;

    /**
     * Resolves the ID of the conversation partner for a given user.
     * 
     * @param myId The ID of the current user
     * @return The ID of the other participant, or null if myId is not a participant
     */
    public Integer getOtherMemberId(Integer myId) {
        if (myId.equals(memId1)) {
            return memId2;
        } else if (myId.equals(memId2)) {
            return memId1;
        }
        return null;
    }
}
