package com.petguardian.chat.service;

import java.util.List;

import com.petguardian.chat.model.ChatMessageDTO;

/**
 * Service Interface for Core Chat Operations.
 * 
 * Defines the contract for:
 * - Handling incoming real-time messages
 * - Retrieving historical conversation data
 */
public interface ChatService {

    /**
     * Processes an incoming message payload.
     * 
     * @param dto The message data transfer object
     * @return The persisted message DTO, enriched with system metadata (ID,
     *         timestamp)
     */
    ChatMessageDTO handleIncomingMessage(ChatMessageDTO dto);

    /**
     * Retrieves a paginated list of historical messages for a specific chatroom.
     * 
     * @param chatroomId    Chatroom Identifier
     * @param currentUserId Requesting User ID (for access control)
     * @param page          Page number (0-based)
     * @param size          Number of records per page
     * @return List of {@link ChatMessageDTO}
     */
    List<ChatMessageDTO> getChatHistory(Integer chatroomId, Integer currentUserId, int page, int size);

    /**
     * Checks if the user has any unread messages across all chatrooms.
     */
    boolean hasUnreadMessages(Integer userId);

    /**
     * Marks a specific chatroom as read for the user.
     */
    void markRoomAsRead(Integer chatroomId, Integer userId);

    /**
     * Finds an existing chatroom between two users with a specific type.
     * Does NOT create a new room if not found.
     * 
     * @param currentUserId Current user ID
     * @param partnerId     Target partner ID
     * @param chatroomType  Room type (0=Service, 1=Product)
     * @return ChatRoomVO if found, null otherwise
     */
    com.petguardian.chat.model.ChatRoomVO findChatroom(Integer currentUserId, Integer partnerId, Integer chatroomType);
}
