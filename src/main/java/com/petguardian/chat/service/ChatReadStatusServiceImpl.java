package com.petguardian.chat.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomEntity;

/**
 * Implementation for managing chat read/unread status.
 */
@Service
public class ChatReadStatusServiceImpl implements ChatReadStatusService {

    private final ChatRoomRepository chatroomRepository;

    public ChatReadStatusServiceImpl(ChatRoomRepository chatroomRepository) {
        this.chatroomRepository = chatroomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUnreadMessages(Integer userId) {
        return chatroomRepository.countUnreadRooms(userId) > 0;
    }

    @Override
    @Transactional
    public void markRoomAsRead(Integer chatroomId, Integer userId) {
        ChatRoomEntity chatroom = chatroomRepository.findById(chatroomId).orElse(null);
        if (chatroom == null) {
            return;
        }

        boolean dirty = false;
        if (userId.equals(chatroom.getMemId1())) {
            chatroom.setMem1LastReadAt(LocalDateTime.now());
            dirty = true;
        } else if (userId.equals(chatroom.getMemId2())) {
            chatroom.setMem2LastReadAt(LocalDateTime.now());
            dirty = true;
        }

        if (dirty) {
            chatroomRepository.save(chatroom);
        }
    }
}
