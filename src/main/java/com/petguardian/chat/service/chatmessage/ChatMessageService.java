package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.service.context.MessageCreationContext;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Domain Facade and Orchestrator for Chat Messages.
 * 
 * Responsibilities:
 * - Strategic Orchestration: Dynamically coordinates internal workers
 * (Persistence & Retrieval)
 * - Pure Encapsulation: Shields upper layers from dual-tier (Redis + MySQL)
 * complexity
 * - Resilience Boundary: Authoritative layer for Distributed Fault Tolerance
 * 
 * Design Principles:
 * - Orchestration over Implementation: Delegates specialized logic to
 * package-private managers
 * - Transparency: Provides a unified API while maintaining strict internal
 * modularity
 * 
 * @see ChatMessagePersistenceManager Strategy for Write-Behind Persistence
 * @see ChatMessageRetrievalManager Strategy for Multi-Tiered History Retrieval
 */
@Slf4j
@Service
public class ChatMessageService {

    private final ChatMessagePersistenceManager persistenceManager;
    private final ChatMessageRetrievalManager retrievalManager;
    private final ChatMessageRepository messageRepository;

    public ChatMessageService(
            ChatMessagePersistenceManager persistenceManager,
            ChatMessageRetrievalManager retrievalManager,
            ChatMessageRepository messageRepository) {
        this.persistenceManager = persistenceManager;
        this.retrievalManager = retrievalManager;
        this.messageRepository = messageRepository;
    }

    // =================================================================================
    // WRITE OPERATIONS
    // =================================================================================

    /**
     * Persists a message using a High Availability strategy.
     * Logic: Orchestrates "Redis Write-Behind" with automatic state
     * synchronization.
     * 
     * @param context Immutable context containing message TSID and content
     * @return The persisted {@link ChatMessageEntity}
     */
    @CircuitBreaker(name = "messageWrite", fallbackMethod = "fallbackSave")
    public ChatMessageEntity save(MessageCreationContext context) {
        return persistenceManager.dispatchWrite(context);
    }

    /**
     * Fallback Strategy for Message Persistence.
     * State: Graceful Degradation (Triggered during Redis outages or circuit OPEN).
     * Dispatches a direct synchronous write to MySQL to ensure absolute data
     * persistence.
     */
    protected ChatMessageEntity fallbackSave(MessageCreationContext context, Throwable t) {
        if (log.isDebugEnabled()) {
            log.debug("[Facade] Triggering fallback for message {}: {}", context.messageId(), t.getMessage());
        }
        return persistenceManager.fallbackWriteToMysql(context, t);
    }

    // =================================================================================
    // READ OPERATIONS
    // =================================================================================

    /**
     * Retrieves historical conversation data using Multi-Tiered Retrieval.
     * Strategy: Primary lookup via Redis (MGET) for speed, with auto-fallback to
     * MySQL.
     * 
     * @param chatroomId Target Chatroom identifier
     * @param pageable   Standard pagination metadata
     * @return List of persisted {@link ChatMessageEntity}
     */
    @CircuitBreaker(name = "messageRead", fallbackMethod = "fallbackHistory")
    public List<ChatMessageEntity> fetchHistory(Integer chatroomId, Pageable pageable) {
        return retrievalManager.fetchHistory(chatroomId, pageable);
    }

    /**
     * Fallback Strategy for Chat History Retrieval.
     * Maintains UI consistency by projecting history directly from MySQL when cache
     * is cold or failed.
     */
    protected List<ChatMessageEntity> fallbackHistory(Integer chatroomId, Pageable pageable, Throwable t) {
        log.warn("[Facade] Redis unavailable for history. Falling back to MySQL. Reason: {}", t.getMessage());
        try {
            return messageRepository.findLatest(chatroomId, pageable);
        } catch (Exception e) {
            log.error("[Facade] MySQL fallback also failed: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Specialized retrieval of a single message by ID.
     */
    @CircuitBreaker(name = "messageRead", fallbackMethod = "fallbackFindById")
    public Optional<ChatMessageEntity> findById(Long messageId) {
        return retrievalManager.findById(messageId);
    }

    /**
     * Fallback Strategy for Single Message Resolution.
     */
    protected Optional<ChatMessageEntity> fallbackFindById(Long messageId, Throwable t) {
        log.warn("[Facade] FindById failed: {}", t.getMessage());
        return Optional.empty();
    }

    /**
     * Batch retrieval of multiple messages by their TSIDs.
     */
    @CircuitBreaker(name = "messageRead", fallbackMethod = "fallbackFindAllById")
    public List<ChatMessageEntity> findAllById(Iterable<Long> messageIds) {
        return retrievalManager.findAllById(messageIds);
    }

    /**
     * Fallback Strategy for Batch Message Resolution.
     */
    protected List<ChatMessageEntity> fallbackFindAllById(Iterable<Long> messageIds, Throwable t) {
        log.warn("[Facade] FindAllById failed: {}", t.getMessage());
        return List.of(); // Return empty immutable list
    }
}
