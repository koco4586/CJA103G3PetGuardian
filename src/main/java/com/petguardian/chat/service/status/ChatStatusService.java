package com.petguardian.chat.service.status;

import com.petguardian.chat.dto.ChatReadReceiptDTO;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.ChatMessageDTO;
import com.petguardian.chat.service.chatroom.ChatRoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

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

        // Publish event to handle broadcast AFTER transaction commit
        // This prevents race conditions where clients query DB before commit finishes
        eventPublisher.publishEvent(new ChatReadReceiptDTO(chatroomId, userId, LocalDateTime.now()));
    }

    /**
     * Handles the read receipt event after the transaction successfully commits.
     * This ensures data consistency for clients querying the DB immediately after
     * receiving the socket event.
     */
    @org.springframework.transaction.event.TransactionalEventListener(phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT)
    public void onReadReceipt(ChatReadReceiptDTO event) {
        broadcastReadReceipt(event.getChatroomId(), event.getReaderId());

        // Notify self (Internal Sync for Header Red Dot)
        messagingTemplate.convertAndSend("/topic/user." + event.getReaderId() + ".read", event);
    }

    // =========================================================================
    // NOTIFICATION OPERATIONS
    // =========================================================================

    /**
     * Broadcasts a read receipt to all subscribers of the chatroom.
     *
     * @param chatroomId Chatroom ID
     * @param readerId   The user who read the room
     */
    public void broadcastReadReceipt(Integer chatroomId, Integer readerId) {
        ChatReadReceiptDTO receipt = new ChatReadReceiptDTO(chatroomId, readerId, LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/chatroom." + chatroomId + ".read", receipt);
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
