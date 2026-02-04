package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

/**
 * Internal Worker for Message Retrieval.
 * Implements Redis-First Read Strategy with MySQL Fallback.
 * Package-Private: Should only be accessed by ChatMessageService.
 */
@Slf4j
@Component
class ChatMessageRetrievalManager {

    private final ChatMessageRepository messageRepository;
    private final ChatMessageCache messageCache;

    public ChatMessageRetrievalManager(
            ChatMessageRepository messageRepository,
            ChatMessageCache messageCache) {
        this.messageRepository = messageRepository;
        this.messageCache = messageCache;
    }

    /**
     * Fetches message history with Redis-first strategy.
     */
    List<ChatMessageEntity> fetchHistory(Integer chatroomId, Pageable pageable) {
        // Redis only caches first page (recent 50 messages)
        if (pageable.getPageNumber() == 0) {
            int pageSize = pageable.getPageSize();
            List<ChatMessageEntity> cached = messageCache.getHistory(chatroomId, pageSize);

            if (cached.size() >= pageSize) {
                log.debug("[Retrieval] Cache HIT for room {}. Returning {} messages.", chatroomId, cached.size());
                return cached;
            }
            log.info("[Retrieval] Cache PARTIAL HIT ({}/{}) for room {}. Falling back to MySQL.",
                    cached.size(), pageSize, chatroomId);
        }

        // Fallback or deeper pages: MySQL
        List<ChatMessageEntity> dbResults = fetchFromDatabase(chatroomId, pageable);

        // Optimization: Write-back to Redis for Page 0 (fire-and-forget)
        if (pageable.getPageNumber() == 0 && !dbResults.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("[Retrieval] Warming up Redis cache for room {} with {} messages.", chatroomId,
                            dbResults.size());
                    messageCache.warmUpHistory(chatroomId, dbResults);
                } catch (Exception e) {
                    log.warn("[Retrieval] Cache warm-up failed for room {}. Skipping. Reason: {}", chatroomId,
                            e.getMessage());
                }
            });
        }

        return dbResults;
    }

    @Transactional(readOnly = true)
    private List<ChatMessageEntity> fetchFromDatabase(Integer chatroomId, Pageable pageable) {
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
