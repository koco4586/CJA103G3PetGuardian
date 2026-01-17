package com.petguardian.chat.service;

import java.util.List;

import com.petguardian.chat.model.ChatMessageDTO;

/**
 * Service interface for core chat business logic.
 */
public interface ChatService {

    /**
     * Processes incoming WebSocket messages.
     * Orchestrates chatroom resolution, persistence, and DTO construction with
     * reply context.
     * 
     * @param dto Incoming message payload
     * @return Persisted message DTO with semantic details (e.g., reply chains)
     */
    ChatMessageDTO handleIncomingMessage(ChatMessageDTO dto);

    /**
     * Retrieves paginated chat history.
     * Implements batch fetching to prevent N+1 queries.
     * 
     * @param chatroomId    Target chatroom identifier
     * @param currentUserId Requesting user identifier
     * @param page          Pagination index (0-based)
     * @param size          Batch size
     * @return List of Message DTOs
     */
    List<ChatMessageDTO> getChatHistory(Integer chatroomId, Integer currentUserId, int page, int size);
}
