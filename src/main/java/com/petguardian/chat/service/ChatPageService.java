package com.petguardian.chat.service;

import java.util.List;

import com.petguardian.chat.model.ChatMemberVO;

/**
 * Service Component for View Data Aggregation.
 * Provides composite data structures for initial page rendering.
 */
public interface ChatPageService {

    /**
     * Retrieves valid chat members for directory listing.
     * 
     * @return List of Member VOs
     */
    List<ChatMemberVO> getAllMembers();

    /**
     * Resolves member identity by ID.
     */
    ChatMemberVO getMember(Integer memId);

    /**
     * Aggregates latest conversation snapshots.
     * 
     * @param currentUserId Context User ID
     * @return Map of Partner ID to Truncated Message Content
     */
    java.util.Map<Integer, String> getLastMessages(Integer currentUserId);
}
