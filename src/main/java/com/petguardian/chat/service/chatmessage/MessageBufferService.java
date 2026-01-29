package com.petguardian.chat.service.chatmessage;

import java.util.List;

/**
 * Service Interface for Message Buffering (Queue Management).
 * 
 * Responsibilities:
 * - Abstracts the underlying Queue technology (Redis, RabbitMQ, etc.)
 * - Manages the lifecycle of the Async Buffer.
 * - Supports Producer (offer) and Consumer (poll) operations.
 * - Handles Sharding logic internally if applicable.
 */
public interface MessageBufferService {

    /**
     * Offers a message to the buffer (Producer).
     * 
     * @param shardId The logical shard (e.g., chatroomId % N)
     * @param context The message data
     */
    void offer(int shardId, MessageCreationContext context);

    /**
     * Polls a batch of messages from the buffer (Consumer).
     * 
     * @param shardId   The target shard
     * @param batchSize Max number of items to retrieve
     * @param timeoutMs Max time to wait (blocking) in milliseconds
     * @return List of messages (empty if timeout reached)
     */
    List<MessageCreationContext> pollBatch(int shardId, int batchSize, long timeoutMs);

    /**
     * Gets the current depth of a specific queue shard.
     * Useful for monitoring and auto-scaling alerts.
     * 
     * @param shardId The target shard
     * @return Number of pending messages
     */
    long getQueueDepth(int shardId);

    /**
     * Puts a batch of failed messages back at the START of the consumer queue.
     * Ensures Order Preservation during connection failures.
     * 
     * @param batch The failed batch to retry first.
     */
    void requeue(int shardId, List<MessageCreationContext> batch);

    default boolean isHealthy() {
        return true;
    }
}
