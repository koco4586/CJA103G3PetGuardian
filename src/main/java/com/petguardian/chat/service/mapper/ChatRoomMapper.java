package com.petguardian.chat.service.mapper;

import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatMemberEntity;
import java.util.Map;

/**
 * Mapper for converting ChatRoom Entities to DTOs.
 * Encapsulates presentation logic like resolving display names and avatars.
 */
public interface ChatRoomMapper {

    /**
     * Converts a ChatRoomEntity to a DTO for the given user context.
     * <p>
     * Requires the partner's display name to be pre-resolved.
     * </p>
     * 
     * @param entity        The chatroom entity
     * @param currentUserId The ID of the user request the data
     * @param partnerName   The display name of the other participant
     * @return Enriched DTO
     */
    ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId, String partnerName);

    /**
     * Converts a ChatRoomEntity to a DTO using pre-loaded member data.
     * <p>
     * Designed for batch processing to avoid N+1 queries.
     * </p>
     * 
     * @param entity           The chatroom entity
     * @param currentUserId    The ID of the user requesting the data
     * @param preloadedMembers A map of member IDs to Member Entities
     * @return Enriched DTO
     */
    ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId, Map<Integer, ChatMemberEntity> preloadedMembers);
}
