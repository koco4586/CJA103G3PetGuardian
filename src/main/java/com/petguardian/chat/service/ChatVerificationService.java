package com.petguardian.chat.service;

import com.petguardian.chat.model.ChatRoomVO;

/**
 * Service for verifying chatroom membership and access rights.
 */
public interface ChatVerificationService {

    /**
     * Verifies that the user is a member of the specified chatroom.
     * 
     * @param chatroomId The chatroom to verify
     * @param userId     The user to check membership for
     * @return ChatRoomVO if user is a member
     * @throws RuntimeException if chatroom not found or user is not a member
     */
    ChatRoomVO verifyMembership(Integer chatroomId, Integer userId);

    /**
     * Checks if the user is a member without throwing an exception.
     * 
     * @param chatroomId The chatroom to check
     * @param userId     The user to check
     * @return true if user is a member, false otherwise
     */
    boolean isMember(Integer chatroomId, Integer userId);
}
