package com.petguardian.chat.service.chatroom;

import java.time.LocalDateTime;
import java.util.List;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.chatmessage.ResilienceSupportStrategyService;

/**
 * Segregated Interface for Chat Room and Member Metadata Writing.
 * Extends ResilienceSupportStrategyService for health and error classification.
 */
public interface ChatRoomMetadataWriter extends ResilienceSupportStrategyService {

    void syncRoomMetadata(Integer chatroomId, String preview, LocalDateTime time, Integer senderId);

    void updateCacheOnly(Integer chatroomId, String preview, LocalDateTime time, Integer senderId);

    void addUserToRoom(Integer userId, Integer chatroomId);

    void updateLastReadAt(Integer chatroomId, Integer userId, LocalDateTime time);

    void updateLastReadAtCacheOnly(Integer chatroomId, Integer userId, LocalDateTime time);

    /**
     * Persists room metadata (Read-Through/Direct Save).
     */
    void saveRoomMetadata(ChatRoomMetadataDTO meta);

    /**
     * Persists member profile (Read-Through/Direct Save).
     */
    void saveMemberProfile(MemberProfileDTO profile);

    /**
     * Persists user room list (Read-Through/Direct Save).
     */
    void saveUserRoomList(Integer userId, List<Integer> roomIds);
}
