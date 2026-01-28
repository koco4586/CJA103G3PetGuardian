package com.petguardian.chat.service.chatmessage;

import java.util.List;

import com.petguardian.chat.model.ChatMessageEntity;

/**
 * Strategy Interface for Message Writing Operations.
 * 
 * Segregated from the main strategy to allow workers to strictly depend on
 * write capabilities.
 */
public interface MessageWriterStrategyService extends ResilienceSupportStrategyService {

    /**
     * Persists a message using the encapsulated context.
     * 
     * @param context Data object containing ID, sender, content, etc.
     * @return Persisted Entity
     */
    ChatMessageEntity save(MessageCreationContext context);

    /**
     * Batch persists multiple messages.
     * Essential for high-throughput async workers.
     * 
     * @param contexts List of message contexts
     */
    void saveAll(List<MessageCreationContext> contexts);

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
}
