package com.petguardian.chat.service.chatmessage;

import java.util.List;
import com.petguardian.chat.model.ChatMemberDTO;
import com.petguardian.chat.model.ChatRoomDTO;

/**
 * Service Interface for Chat View Data Aggregation.
 * 
 * Defines the contract for preparing data required to render the initial chat
 * interface,
 * optimized for minimizing network round-trips.
 */
public interface ChatPageService {

    /**
     * Resolves member details by unique identifier.
     * 
     * @param memId Member ID
     * @return Member DTO, or null if not found
     */
    ChatMemberDTO getMember(Integer memId);

    /**
     * Retrieves a summarized list of all chatrooms for the sidebar.
     * Sorted by latest activity.
     * 
     * @param currentUserId Context User ID
     * @return List of summarized chatroom DTOs
     */
    List<ChatRoomDTO> getMyChatrooms(Integer currentUserId);
}
