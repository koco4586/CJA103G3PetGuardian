package com.petguardian.chat.service;

import java.util.List;
import java.util.Map;

import com.petguardian.chat.model.ChatMemberDTO;

/**
 * Service Interface for Chat View Data Aggregation.
 * 
 * Defines the contract for preparing data required to render the initial chat
 * interface,
 * optimized for minimizing network round-trips.
 */
public interface ChatPageService {

    /**
     * Retrieves the complete directory of chat-enabled members.
     * 
     * @return List of Member DTOs
     */
    List<ChatMemberDTO> getAllMembers();

    /**
     * Resolves member details by unique identifier.
     * 
     * @param memId Member ID
     * @return Member DTO, or null if not found
     */
    ChatMemberDTO getMember(Integer memId);

    /**
     * Compiles a summary of recent conversations for the current user.
     * Returns a map of partner IDs to their latest message preview.
     * 
     * @param currentUserId Context User ID
     * @return {@code Map<PartnerId, PreviewText>}
     */
    Map<Integer, String> getLastMessages(Integer currentUserId);
}
