package com.petguardian.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomEntity;

/**
 * Implementation for verifying chatroom membership and access rights.
 */
@Service
public class ChatVerificationServiceImpl implements ChatVerificationService {

    private final ChatRoomRepository chatroomRepository;

    public ChatVerificationServiceImpl(ChatRoomRepository chatroomRepository) {
        this.chatroomRepository = chatroomRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoomEntity verifyMembership(Integer chatroomId, Integer userId) {
        ChatRoomEntity chatroom = chatroomRepository.findById(chatroomId).orElse(null);
        if (chatroom == null) {
            throw new IllegalArgumentException("Chatroom not found: " + chatroomId);
        }
        if (!isMemberInternal(chatroom, userId)) {
            throw new SecurityException("Access denied: User " + userId + " is not a member of chatroom " + chatroomId);
        }
        return chatroom;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(Integer chatroomId, Integer userId) {
        ChatRoomEntity chatroom = chatroomRepository.findById(chatroomId).orElse(null);
        if (chatroom == null) {
            return false;
        }
        return isMemberInternal(chatroom, userId);
    }

    private boolean isMemberInternal(ChatRoomEntity chatroom, Integer userId) {
        return userId.equals(chatroom.getMemId1()) || userId.equals(chatroom.getMemId2());
    }
}
