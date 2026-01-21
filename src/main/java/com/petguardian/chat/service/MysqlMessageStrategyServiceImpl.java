package com.petguardian.chat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.model.ChatMessageEntity;

import io.hypersistence.tsid.TSID;

/**
 * MySQL Strategy Implementation for Message Operations.
 * 
 * Characteristics:
 * - Direct synchronous writes to MySQL (ACID compliant)
 * - Updates chatroom metadata (lastMessageAt, preview) in same transaction
 * - Uses TSID for ID generation to support future distributed migration
 * 
 * Note: Marked as @Primary until Redis implementation is active.
 */
@Service
@Primary
public class MysqlMessageStrategyServiceImpl implements MessageStrategyService {

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private final ChatMessageRepository messageRepository;
    private final TSID.Factory tsidFactory;

    public MysqlMessageStrategyServiceImpl(ChatMessageRepository messageRepository,
            TSID.Factory tsidFactory) {
        this.messageRepository = messageRepository;
        this.tsidFactory = tsidFactory;
    }

    // ============================================================
    // WRITE OPERATIONS
    // ============================================================

    /**
     * Persists a message and updates the chatroom status atomically.
     */
    @Override
    @Transactional
    public ChatMessageEntity save(Integer chatroomId, Integer senderId, String content, String replyToId) {
        // Generate distributed-safe ID (TSID)
        String messageId = tsidFactory.generate().toString();

        ChatMessageEntity message = new ChatMessageEntity();
        message.setMessageId(messageId);
        message.setChatroomId(chatroomId);
        message.setMemberId(senderId);
        message.setMessage(content);

        if (replyToId != null) {
            message.setReplyToMessageId(replyToId);
        }

        ChatMessageEntity savedMessage = messageRepository.save(message);

        return savedMessage;
    }

    // ============================================================
    // READ OPERATIONS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> findLatestMessages(Integer chatroomId, Pageable pageable) {
        return messageRepository.findLatest(chatroomId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatMessageEntity> findById(String messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> findAllById(Iterable<String> messageIds) {
        return messageRepository.findAllById(messageIds);
    }
}
