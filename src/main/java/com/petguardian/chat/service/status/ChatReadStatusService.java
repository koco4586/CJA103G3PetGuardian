package com.petguardian.chat.service.status;

/**
 * Service for managing chat read/unread status.
 */
public interface ChatReadStatusService {

    /**
     * Checks if the user has any unread messages across all chatrooms.
     */
    boolean hasUnreadMessages(Integer userId);

    /**
     * Marks a specific chatroom as read for the user.
     */
    void markRoomAsRead(Integer chatroomId, Integer userId);
}
