package com.petguardian.chat.service.status;

import com.petguardian.chat.dto.ChatMessageDTO;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.service.chatroom.ChatRoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Unified Facade for Chat Status Operations.
 * 
 * Responsibilities:
 * 1. Read Status Management: Query and update last-read timestamps
 * 2. WebSocket Notifications: Broadcast read receipts and messages
 * 
 * Design:
 * - Single entry point for all status-related operations
 * - Encapsulates SimpMessagingTemplate from ChatServiceImpl
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatStatusService {

    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    // =========================================================================
    // READ STATUS OPERATIONS
    // =========================================================================

    /**
     * Checks if the user has any unread messages across all chatrooms.
     *
     * @param userId User ID
     * @return true if there are unread messages
     */
    @Transactional(readOnly = true)
    public boolean hasUnreadMessages(Integer userId) {
        List<ChatRoomMetadataDTO> rooms = chatRoomService.getUserChatroomMetadata(userId);
        if (rooms == null || rooms.isEmpty()) {
            return false;
        }

        return rooms.stream().anyMatch(meta -> {
            if (meta.getLastMessageAt() == null) {
                return false;
            }

            LocalDateTime myLastRead = userId.equals(meta.getMemberIds().get(0))
                    ? meta.getMem1LastReadAt()
                    : meta.getMem2LastReadAt();

            return myLastRead == null || meta.getLastMessageAt().isAfter(myLastRead);
        });
    }

    /**
     * Marks a chatroom as read for the user.
     * Also broadcasts a read receipt via WebSocket.
     *
     * @param chatroomId Chatroom ID
     * @param userId     User ID
     */
    @Transactional
    public void markRoomAsRead(Integer chatroomId, Integer userId) {
        chatRoomService.updateLastReadAt(chatroomId, userId, LocalDateTime.now());
        broadcastReadReceipt(chatroomId);
    }

    // =========================================================================
    // NOTIFICATION OPERATIONS
    // =========================================================================

    /**
     * Broadcasts a read receipt to all subscribers of the chatroom.
     *
     * @param chatroomId Chatroom ID
     */
    public void broadcastReadReceipt(Integer chatroomId) {
        ChatMessageDTO readReceipt = new ChatMessageDTO();
        readReceipt.setChatroomId(chatroomId);
        readReceipt.setIsRead(true);
        messagingTemplate.convertAndSend("/topic/chatroom." + chatroomId + ".read", readReceipt);
    }

    /**
     * Broadcasts a new message to all subscribers of the chatroom.
     *
     * @param chatroomId Chatroom ID
     * @param message    Message DTO to broadcast
     */
    public void broadcastMessage(Integer chatroomId, ChatMessageDTO message) {
        messagingTemplate.convertAndSend("/topic/chatroom." + chatroomId, message);
    }
}
