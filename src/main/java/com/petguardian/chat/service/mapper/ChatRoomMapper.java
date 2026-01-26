package com.petguardian.chat.service.mapper;

import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;

/**
 * Mapper for converting ChatRoom Entities to DTOs.
 * Encapsulates presentation logic like resolving display names and avatars.
 */
public interface ChatRoomMapper {

    /**
     * Converts a ChatRoomEntity to a DTO for the given user context.
     * <p>
     * This method fetches member details from the repository individually (N+1
     * risk).
     * Use {@link #toDto(ChatRoomEntity, Integer, java.util.Map)} for batch
     * processing.
     * </p>
     * 
     * @param entity        The chatroom entity
     * @param currentUserId The ID of the user request the data
     * @return Enriched DTO
     */
    ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId);

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
    ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId,
            java.util.Map<Integer, com.petguardian.chat.model.ChatMemberEntity> preloadedMembers);
}
