package com.petguardian.chat.service.chatroom;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;

/**
 * Segregated Interface for Chat Room and Member Metadata Reading.
 */
public interface ChatRoomMetadataReader {

    ChatRoomMetadataDTO getRoomMetadata(Integer chatroomId);

    MemberProfileDTO getMemberProfile(Integer memberId);

    Map<Integer, MemberProfileDTO> getMemberProfiles(List<Integer> memberIds);

    List<ChatRoomMetadataDTO> getUserChatrooms(Integer userId);

    Optional<ChatRoomMetadataDTO> findRoomByMembers(Integer memId1, Integer memId2);
}
