package com.petguardian.chat.service;

import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;

/**
 * Mapper for converting ChatRoom Entities to DTOs.
 * Encapsulates presentation logic like resolving display names and avatars.
 */
public interface ChatRoomMapper {

    /**
     * Converts a ChatRoomEntity to a DTO for the given user context.
     * 
     * @param entity        The chatroom entity
     * @param currentUserId The ID of the user request the data
     * @return Enriched DTO
     */
    ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId);
}
