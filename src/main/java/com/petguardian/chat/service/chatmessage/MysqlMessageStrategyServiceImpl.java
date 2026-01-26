package com.petguardian.chat.service.chatmessage;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatRoomRepository;

// Context in same package

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
@Service("mysqlStrategy")
@Primary // Temporary until Proxy is implemented
public class MysqlMessageStrategyServiceImpl implements MessageStrategyService {

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public MysqlMessageStrategyServiceImpl(ChatMessageRepository messageRepository,
            ChatRoomRepository chatRoomRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    // ============================================================
    // WRITE OPERATIONS
    // ============================================================

    /**
     * Persists a message and updates the chatroom status atomically.
     */
    @Override
    @Transactional
    public ChatMessageEntity save(MessageCreationContext context) {
        // Use application-provided ID
        ChatMessageEntity message = new ChatMessageEntity();
        message.setMessageId(context.messageId());
        message.setChatroomId(context.chatroomId());
        message.setMemberId(context.senderId());
        message.setMessage(context.content());

        if (context.replyToId() != null) {
            message.setReplyToMessageId(context.replyToId());
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

    // ============================================================
    // METADATA OPERATIONS
    // ============================================================

    @Override
    @Transactional
    public void updateRoomMetadata(Integer chatroomId, Integer senderId, String content) {
        // We fetch the entity - in MySQL this acquires a lock if in a transaction
        // For Redis/Async strategy, this entire method will look different (Hash op)
        chatRoomRepository.findById(chatroomId).ifPresent(chatroom -> {
            chatroom.setLastMessageAt(LocalDateTime.now());
            // Truncate preview
            String preview = content.length() > 200 ? content.substring(0, 200) : content;
            chatroom.setLastMessagePreview(preview);

            // Update sender's read status
            chatroom.updateLastReadAt(senderId);

            chatRoomRepository.save(chatroom);
        });
    }
}
