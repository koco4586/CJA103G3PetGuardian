package com.petguardian.chat.service.chatmessage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.petguardian.chat.model.ChatMessageEntity;
// Context in same package

/**
 * Strategy Interface for Unified Message Operations.
 * 
 * Implements the Strategy Pattern to decouple the service layer from
 * underlying storage implementations.
 * 
 * Supported Strategies:
 * 1. Synchronous MySQL (Current Primary)
 * - Direct ACID transactions
 * 2. Asynchronous Redis Write-Behind (Future)
 * - High-throughput buffering with eventual consistence
 */
public interface MessageStrategyService {

    // ============================================================
    // WRITE OPERATIONS
    // ============================================================

    /**
     * Persists a message using the encapsulated context.
     * 
     * @param context Data object containing ID, sender, content, etc.
     * @return Persisted Entity
     */
    ChatMessageEntity save(MessageCreationContext context);

    /**
     * Triggers manual flush of buffered messages (Async strategies only).
     * 
     * @return Number of flushed records
     */
    default int flushToPersistence() {
        return 0;
    }

    /**
     * Returns count of messages waiting in buffer (Async strategies only).
     */
    default int getPendingCount() {
        return 0;
    }

    /**
     * Indicates if the current strategy buffers writes.
     */
    default boolean isAsyncPersistence() {
        return false;
    }

    // ============================================================
    // READ OPERATIONS
    // ============================================================

    /**
     * Retrieves latest messages for a chatroom using the most efficient
     * read path (e.g., Cache Hit -> DB Miss).
     */
    List<ChatMessageEntity> findLatestMessages(Integer chatroomId, Pageable pageable);

    /**
     * Resolves a single message by ID.
     */
    Optional<ChatMessageEntity> findById(String messageId);

    /**
     * Batch resolves messages by ID.
     * Used typically for resolving reply contexts in bulk.
     */
    List<ChatMessageEntity> findAllById(Iterable<String> messageIds);
}
