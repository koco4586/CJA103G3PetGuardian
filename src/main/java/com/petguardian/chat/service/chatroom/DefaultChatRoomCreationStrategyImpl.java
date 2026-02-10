package com.petguardian.chat.service.chatroom;

import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;

/**
 * Default implementation for 1-on-1 chatroom creation.
 * Uses normalized ID order to prevent duplicate chatrooms.
 */
@Slf4j
@Service
public class DefaultChatRoomCreationStrategyImpl implements ChatRoomCreationStrategy {

    private final ChatRoomRepository chatroomRepository;
    private final ChatRoomMetadataService metadataService;

    public DefaultChatRoomCreationStrategyImpl(ChatRoomRepository chatroomRepository,
            ChatRoomMetadataService metadataService) {
        this.chatroomRepository = chatroomRepository;
        this.metadataService = metadataService;
    }

    @Override
    public ChatRoomEntity findOrCreate(Integer userA, Integer userB, Integer chatroomType) {
        // Normalized Lookup (smaller ID first)
        Integer memId1 = Math.min(userA, userB);
        Integer memId2 = Math.max(userA, userB);
        Integer type = chatroomType != null ? chatroomType : 0;

        // High-Performance Lookup: Cache First via Metadata Service (Type-Aware)
        Optional<ChatRoomMetadataDTO> cachedRoom = metadataService
                .findRoomByMembersAndType(memId1, memId2, type);

        if (cachedRoom.isPresent()) {
            ChatRoomMetadataDTO meta = cachedRoom.get();
            // Double check type consistency just in case
            if (meta.getChatroomType().intValue() == type.intValue()) {
                ChatRoomEntity entity = new ChatRoomEntity();
                entity.setChatroomId(meta.getChatroomId());
                entity.setMemId1(memId1);
                entity.setMemId2(memId2);
                entity.setChatroomType(type.byteValue());
                return entity;
            }
        }

        // Fallback: Secondary Storage Lookup (Cold Start)
        Optional<ChatRoomEntity> existingRoom = chatroomRepository.findByMemId1AndMemId2AndChatroomType(memId1, memId2,
                type);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Creation (Ordered IDs: smaller first)
        ChatRoomEntity newRoom = new ChatRoomEntity();
        newRoom.setMemId1(Math.min(userA, userB));
        newRoom.setMemId2(Math.max(userA, userB));

        // Context Type (Default to 0 if null)
        newRoom.setChatroomType((byte) (chatroomType != null ? chatroomType.intValue() : 0));

        // Default name
        newRoom.setChatroomName("Chat: " + newRoom.getMemId1() + "-" + newRoom.getMemId2());

        ChatRoomEntity saved;
        try {
            saved = chatroomRepository.save(newRoom);
        } catch (DataIntegrityViolationException e) {
            // Race condition: Another transaction created this room concurrently.
            // Retry lookup from DB (the winning transaction's data is now committed).
            log.debug("[RoomCreator] Concurrent creation detected for ({},{},{}). Retrying lookup.",
                    memId1, memId2, type);
            return chatroomRepository.findByMemId1AndMemId2AndChatroomType(memId1, memId2, type)
                    .orElseThrow(() -> new IllegalStateException(
                            "Constraint violation but room not found: " + memId1 + "," + memId2, e));
        }

        // Push to Cache List & Lookup Cache
        metadataService.addUserToRoom(saved.getMemId1(), saved.getChatroomId());
        metadataService.addUserToRoom(saved.getMemId2(), saved.getChatroomId());

        // Type-Aware Cache Set
        metadataService.cacheRoomLookup(saved.getMemId1(), saved.getMemId2(), type, saved.getChatroomId());
        // Note: The above cacheRoomLookup is deprecated and likely only sets the
        // type-less or default type key
        return saved;
    }

    @Override
    public void updateName(Integer chatroomId, String newName) {
        chatroomRepository.findById(chatroomId).ifPresent(room -> {
            room.setChatroomName(newName);
            chatroomRepository.save(room);
        });
    }

    @Override
    public boolean supports(String type) {
        return "DEFAULT".equalsIgnoreCase(type) || type == null;
    }
}
