package com.petguardian.chat.service;

import java.util.List;
import java.util.Map;

import com.petguardian.chat.dto.ChatMessageDTO;

/**
 * Service Interface for Core Chat Message Operations.
 * Defines the contract for:
 * - Handling incoming real-time messages with Zero-SQL pre-loading
 * - Orchestrating historical conversation retrieval with pagination
 * - Executing keyword-based search via specialized Retrieval Strategy
 * - Managing real-time synchronization of read status
 */
public interface ChatService {

    /**
     * Processes an incoming message payload.
     * 
     * @param dto The message data transfer object
     * @return The persisted message DTO, enriched with context metadata (TSID,
     *         sender profiles, and timestamps)
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

    /**
     * Executes a keyword search within the chatroom's historical context.
     * Results are enriched with full sender and reply metadata.
     * 
     * @param chatroomId  The target chatroom identifier
     * @param keyword     The search query
     * @param requesterId The user performing the search (for consistency checks)
     * @return Enriched list of matching {@link ChatMessageDTO}
     */
    List<ChatMessageDTO> searchChatHistory(Integer chatroomId, String keyword, Integer requesterId);

    /**
     * Calculates the exact page position of a specific message in the timeline.
     * Used for the "Jump to Message" functionality to maintain UX context.
     * 
     * @param chatroomId Chatroom Identifier
     * @param messageId  Target Message TSID (String format)
     * @param pageSize   Standard pagination size
     * @return A map containing the calculated 'page' index
     */
    Map<String, Integer> getMessagePosition(Integer chatroomId, String messageId, Integer pageSize);
}
