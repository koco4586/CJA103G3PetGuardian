package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Internal Worker for Message Retrieval.
 * Handles reading from Cache (Future) or DB.
 * Package-Private: Should only be accessed by ChatMessageService.
 */
@Component
class ChatMessageRetrievalManager {

    private final ChatMessageRepository messageRepository;

    public ChatMessageRetrievalManager(ChatMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    List<ChatMessageEntity> fetchHistory(Integer chatroomId, Pageable pageable) {
        // Future: Check Redis Cache for first page?
        return messageRepository.findLatest(chatroomId, pageable);
    }

    @Transactional(readOnly = true)
    Optional<ChatMessageEntity> findById(String messageId) {
        return messageRepository.findById(messageId);
    }

    @Transactional(readOnly = true)
    List<ChatMessageEntity> findAllById(Iterable<String> messageIds) {
        return messageRepository.findAllById(messageIds);
    }
}
