package com.petguardian.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.model.ChatMessageVO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomVO;

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
    private final ChatRoomRepository chatroomRepository;
    private final TSID.Factory tsidFactory;

    public MysqlMessageStrategyServiceImpl(ChatMessageRepository messageRepository,
            ChatRoomRepository chatroomRepository,
            TSID.Factory tsidFactory) {
        this.messageRepository = messageRepository;
        this.chatroomRepository = chatroomRepository;
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
    public ChatMessageVO save(Integer chatroomId, Integer senderId, String content, String replyToId) {
        // Generate distributed-safe ID (TSID)
        String messageId = tsidFactory.generate().toString();

        ChatMessageVO message = new ChatMessageVO();
        message.setMessageId(messageId);
        message.setChatroomId(chatroomId);
        message.setMemberId(senderId);
        message.setMessage(content);

        if (replyToId != null) {
            message.setReplyToMessageId(replyToId);
        }

        ChatMessageVO savedMessage = messageRepository.save(message);

        // Update chatroom metadata for sidebar preview optimization
        ChatRoomVO chatroom = chatroomRepository.findById(chatroomId).orElse(null);
        if (chatroom != null) {
            chatroom.setLastMessageAt(LocalDateTime.now());
            // Truncate preview to fit DB column
            String preview = content.length() > 200 ? content.substring(0, 200) : content;
            chatroom.setLastMessagePreview(preview);
            chatroomRepository.save(chatroom);
        }

        return savedMessage;
    }

    // ============================================================
    // READ OPERATIONS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageVO> findLatestMessages(Integer chatroomId, Pageable pageable) {
        return messageRepository.findLatest(chatroomId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatMessageVO> findById(String messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageVO> findAllById(Iterable<String> messageIds) {
        return messageRepository.findAllById(messageIds);
    }
}
