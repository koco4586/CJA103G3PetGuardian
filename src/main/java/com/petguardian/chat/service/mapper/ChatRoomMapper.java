package com.petguardian.chat.service.mapper;

import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.model.ChatMemberEntity;
import com.petguardian.chat.model.ChatRoomEntity;

import java.util.Map;

/**
 * Mapper for ChatRoom domain conversions.
 * Responsibilities:
 * Entity to DTO conversion (view layer)
 * Entity to MetadataDTO conversion (caching layer)
 * Presentation logic (display names, room tags, unread status)
 * This mapper centralizes all chatroom mapping logic
 */
public interface ChatRoomMapper {

        // =========================================================================
        // VIEW LAYER CONVERSIONS (Entity/Metadata -> ChatRoomDTO)
        // =========================================================================

        /**
         * Converts a ChatRoomEntity to a ChatRoomDTO for UI rendering.
         *
         * @param entity        Source entity
         * @param currentUserId Current user context
         * @param partnerName   Pre-resolved partner display name
         * @return ChatRoomDTO for sidebar display
         */
        ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId, String partnerName);

        /**
         * Converts a ChatRoomEntity using pre-loaded member data (batch optimization).
         */
        ChatRoomDTO toDto(ChatRoomEntity entity, Integer currentUserId,
                        Map<Integer, ChatMemberEntity> preloadedMembers);

        /**
         * Converts a ChatRoomMetadataDTO to ChatRoomDTO using pre-loaded profiles.
         */
        ChatRoomDTO toDtoFromMeta(ChatRoomMetadataDTO meta, Integer currentUserId,
                        Map<Integer, MemberProfileDTO> preloadedProfiles);

        // =========================================================================
        // CACHE LAYER CONVERSIONS (Entity -> MetadataDTO)
        // =========================================================================

        /**
         * Converts a ChatRoomEntity to ChatRoomMetadataDTO for caching.
         * This method centralizes the Entity-to-Metadata mapping previously
         * duplicated in ChatRoomMetadataService.
         * 
         * @param entity Source entity from database
         * @return MetadataDTO suitable for Redis caching
         */
        ChatRoomMetadataDTO toMetadataDto(ChatRoomEntity entity);

        /**
         * Converts a ChatMemberEntity to a MemberProfileDTO.
         * 
         * @param entity Source member entity
         * @return Standardized member profile DTO
         */
        MemberProfileDTO toMemberProfileDto(ChatMemberEntity entity);
}
