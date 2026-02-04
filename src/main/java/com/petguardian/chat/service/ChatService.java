package com.petguardian.chat.service;

import java.util.List;

import com.petguardian.chat.service.chatroom.ChatRoomService;
import com.petguardian.chat.dto.ChatMessageDTO;

/**
 * Service Interface for Core Chat Message Operations.
 * Defines the contract for:
 * Handling incoming real-time messages
 * Retrieving historical conversation data
 * Managing read status
 * Note: Chatroom creation/lookup is handled by
 * {@link ChatRoomService}.
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
     *
     * @param userId User ID
     * @return true if there are unread messages
     */
    boolean hasUnreadMessages(Integer userId);

    /**
     * Marks a specific chatroom as read for the user.
     *
     * @param chatroomId Chatroom ID
     * @param userId     User ID
     */
    void markRoomAsRead(Integer chatroomId, Integer userId);
}
