package com.petguardian.chat.service;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.model.ChatMessageVO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomVO;

/**
 * MySQL implementation of MessageStoreStrategyService.
 * Saves message and updates chatroom's lastMessageAt in same transaction.
 */
@Service
@Primary
public class MessageStoreStrategyServiceImpl implements MessageStoreStrategyService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository chatroomRepository;

    public MessageStoreStrategyServiceImpl(ChatMessageRepository messageRepository,
            ChatRoomRepository chatroomRepository) {
        this.messageRepository = messageRepository;
        this.chatroomRepository = chatroomRepository;
    }

    @Override
    @Transactional
    public ChatMessageVO save(Integer chatroomId, Integer senderId, String content, Integer replyToId) {
        // 1. Create and save message
        ChatMessageVO message = new ChatMessageVO();
        message.setChatroomId(chatroomId);
        message.setMemberId(senderId);
        message.setMessage(content);

        if (replyToId != null) {
            message.setReplyToMessageId(replyToId);
        }

        ChatMessageVO savedMessage = messageRepository.save(message);

        // 2. Update chatroom's lastMessageAt and lastMessagePreview
        ChatRoomVO chatroom = chatroomRepository.findById(chatroomId).orElse(null);
        if (chatroom != null) {
            chatroom.setLastMessageAt(LocalDateTime.now());
            // Truncate preview to 200 chars if needed
            String preview = content.length() > 200 ? content.substring(0, 200) : content;
            chatroom.setLastMessagePreview(preview);
            chatroomRepository.save(chatroom);
        }

        return savedMessage;
    }
}
