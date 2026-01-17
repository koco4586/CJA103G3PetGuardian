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

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 200)
    private String lastMessagePreview;

    /**
     * Helper method: Get the other member's ID in this chatroom
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
