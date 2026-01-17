package com.petguardian.chat.service;

import java.util.List;

import com.petguardian.chat.model.ChatMessageVO;

/**
 * Strategy Service interface for message persistence (Strategy Pattern).
 * Supports multiple storage strategies:
 * 
 * 1. MySQL Direct (MessageStoreStrategyServiceImpl - current @Primary)
 * - Synchronous write to MySQL
 * - Simple, reliable, suitable for low traffic
 * 
 * 2. Redis Write-Behind (future implementation)
 * - Fast write to Redis first
 * - Async batch persist to MySQL via scheduled job
 * - High throughput for concurrent chat
 */
public interface MessageStoreStrategyService {

    /**
     * Save a chat message and return the persisted entity.
     * Implementation may write to Redis, MySQL, or both depending on strategy.
     * 
     * @param chatroomId the chatroom ID
     * @param senderId   the sender's member ID
     * @param content    the message content
     * @return the saved ChatMessageVO (may have temporary ID if async)
     */
    default ChatMessageVO save(Integer chatroomId, Integer senderId, String content) {
        return save(chatroomId, senderId, content, null);
    }

    /**
     * Save a chat message with optional reply reference.
     * 
     * @param chatroomId the chatroom ID
     * @param senderId   the sender's member ID
     * @param content    the message content
     * @param replyToId  the ID of the message being replied to (nullable)
     * @return the saved ChatMessageVO
     */
    ChatMessageVO save(Integer chatroomId, Integer senderId, String content, Integer replyToId);

    /**
     * Flush pending messages to persistent storage.
     * For Redis Write-Behind: batch write cached messages to MySQL.
     * For MySQL Direct: no-op (already persisted).
     * 
     * @return number of messages flushed
     */
    default int flushToPersistence() {
        return 0; // Default: no-op for sync implementations
    }

    /**
     * Get pending message count waiting for persistence.
     * For Redis Write-Behind: count of messages in Redis not yet in MySQL.
     * For MySQL Direct: always 0.
     * 
     * @return pending count
     */
    default int getPendingCount() {
        return 0; // Default: 0 for sync implementations
    }

    /**
     * Check if this strategy uses async persistence.
     * 
     * @return true if messages are buffered before MySQL persistence
     */
    default boolean isAsyncPersistence() {
        return false; // Default: sync for MySQL Direct
    }

    /**
     * Get recent messages from fast storage (Redis cache or MySQL).
     * For Redis Write-Behind: read from Redis first for speed.
     * For MySQL Direct: read from MySQL.
     * 
     * @param chatroomId the chatroom ID
     * @param limit      max number of messages
     * @return list of recent messages
     */
    default List<ChatMessageVO> getRecentMessages(Integer chatroomId, int limit) {
        return List.of(); // Override in implementations
    }
}
