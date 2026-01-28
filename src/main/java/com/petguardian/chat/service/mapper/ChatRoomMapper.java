package com.petguardian.chat.service.mapper;

import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatMemberEntity;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import java.util.Map;

/**
 * Mapper for converting ChatRoom Entities or Metadata to DTOs.
 * Encapsulates presentation logic like resolving display names and avatars.
 */
public interface ChatRoomMapper {

    /**
     * Converts a ChatRoomEntity to a DTO for the given user context.
     */
    ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId, String partnerName);

    /**
     * Converts a ChatRoomEntity to a DTO using pre-loaded member data.
     */
    ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId, Map<Integer, ChatMemberEntity> preloadedMembers);

    /**
     * Converts a ChatRoomMetadataDTO DTO to a ChatRoomDTO using pre-loaded member
     * profiles.
     */
    ChatRoomDTO toDtoFromMeta(ChatRoomMetadataDTO meta, Integer currentUserId,
            Map<Integer, MemberProfileDTO> preloadedProfiles);
}
