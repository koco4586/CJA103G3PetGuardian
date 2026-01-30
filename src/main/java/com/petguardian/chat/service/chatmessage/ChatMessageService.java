package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.model.ChatMessageEntity;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Domain Facade for Chat Messages.
 * 
 * Responsibilities:
 * 1. Unified Entry Point for all Message Data Operations.
 * 2. Resilience Aspect Boundary (@CircuitBreaker).
 * 3. Coordinates Internal Workers (PersistenceManager, RetrievalManager).
 * 
 * Design:
 * - Pure Encapsulation: Controllers and ChatServiceImpl do not know about
 * Redis/MySQL.
 * - Thin Facade: Delegates implementation to package-private managers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessagePersistenceManager persistenceManager;
    private final ChatMessageRetrievalManager retrievalManager;

    // =================================================================================
    // WRITE OPERATIONS
    // =================================================================================

    /**
     * Persists a message with High Availability.
     * Uses "Redis Write-Behind" by default, falls back to "MySQL Direct Write".
     */
    @CircuitBreaker(name = "messageWrite", fallbackMethod = "fallbackSave")
    public ChatMessageEntity save(MessageCreationContext context) {
        return persistenceManager.dispatchWrite(context);
    }

    /**
     * Fallback for save() when Redis is down/unstable.
     * Guaranteed to be called by Resilience4j on open circuit or exception.
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
     * Retrieves chat history with High Availability.
     */
    @CircuitBreaker(name = "messageRead", fallbackMethod = "fallbackHistory")
    public List<ChatMessageEntity> fetchHistory(Integer chatroomId, Pageable pageable) {
        return retrievalManager.fetchHistory(chatroomId, pageable);
    }

    protected List<ChatMessageEntity> fallbackHistory(Integer chatroomId, Pageable pageable, Throwable t) {
        log.error("[Facade] History retrieval failed: {}", t.getMessage());
        // In retrieval, fallback might be returning empty list or rethrowing.
        // For now, we propagate or empty.
        throw new RuntimeException("Chat service temporarily unavailable for history", t);
    }

    @CircuitBreaker(name = "messageRead", fallbackMethod = "fallbackFindById")
    public Optional<ChatMessageEntity> findById(String messageId) {
        return retrievalManager.findById(messageId);
    }

    protected Optional<ChatMessageEntity> fallbackFindById(String messageId, Throwable t) {
        log.warn("[Facade] FindById failed: {}", t.getMessage());
        return Optional.empty();
    }

    @CircuitBreaker(name = "messageRead", fallbackMethod = "fallbackFindAllById")
    public List<ChatMessageEntity> findAllById(Iterable<String> messageIds) {
        return retrievalManager.findAllById(messageIds);
    }

    protected List<ChatMessageEntity> fallbackFindAllById(Iterable<String> messageIds, Throwable t) {
        log.warn("[Facade] FindAllById failed: {}", t.getMessage());
        return List.of(); // Return empty immutable list
    }
}
