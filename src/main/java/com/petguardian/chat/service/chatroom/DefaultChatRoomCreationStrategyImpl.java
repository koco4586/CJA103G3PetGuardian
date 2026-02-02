package com.petguardian.chat.service.chatroom;

import java.util.Optional;
import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;

/**
 * Default implementation for 1-on-1 chatroom creation.
 * Uses normalized ID order to prevent duplicate chatrooms.
 */
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

        // High-Performance Lookup: Cache First via Metadata Service
        Optional<ChatRoomMetadataDTO> cachedRoom = metadataService
                .findRoomByMembers(memId1, memId2);
        if (cachedRoom.isPresent()) {
            ChatRoomMetadataDTO meta = cachedRoom.get();
            ChatRoomEntity entity = new ChatRoomEntity();
            entity.setChatroomId(meta.getChatroomId());
            entity.setMemId1(memId1);
            entity.setMemId2(memId2);
            entity.setChatroomType(type.byteValue());
            return entity;
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

        ChatRoomEntity saved = chatroomRepository.save(newRoom);

        // Push to Cache List & Lookup Cache
        metadataService.addUserToRoom(saved.getMemId1(), saved.getChatroomId());
        metadataService.addUserToRoom(saved.getMemId2(), saved.getChatroomId());
        metadataService.cacheRoomLookup(saved.getMemId1(), saved.getMemId2(), saved.getChatroomId());

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
