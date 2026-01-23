package com.petguardian.chat.service.chatroom;

import com.petguardian.chat.model.ChatRoomEntity;

/**
 * Strategy interface for chatroom creation.
 * Allows different logic for creating chatrooms (e.g., 1-on-1, Group, etc.).
 */
public interface ChatRoomCreationStrategy {

    /**
     * Resolves or creates a chatroom for the given users with a specific type.
     *
     * @param userA        First user ID
     * @param userB        Second user ID
     * @param chatroomType The type of the chatroom (e.g., 0=Service, 1=Product)
     * @return The resolved or created ChatRoomEntity
     */
    ChatRoomEntity findOrCreate(Integer userA, Integer userB, Integer chatroomType);

    /**
     * Updates the name of an existing chatroom.
     * 
     * @param chatroomId The ID of the chatroom to update
     * @param newName    The new name for the chatroom
     */
    void updateName(Integer chatroomId, String newName);

    /**
     * Checks if this strategy supports the given strategy identifier.
     * 
     * @param strategyType Identifier for strategy type
     * @return true if supported
     */
    boolean supports(String strategyType);
}
