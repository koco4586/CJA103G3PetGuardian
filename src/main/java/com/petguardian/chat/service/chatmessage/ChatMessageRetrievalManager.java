package com.petguardian.chat.service.chatmessage;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.dto.ChatMessageRedisDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
@Service
@RequiredArgsConstructor
public class ChatMessageRetrievalManager {

    private final ChatMessageRepository messageRepository;
    private final ChatMessageCache messageCache;

    /**
     * Fetches message history with Redis-first strategy.
     */
    List<ChatMessageEntity> fetchHistory(Integer chatroomId, Pageable pageable) {
        // Redis only caches first page (recent 50 messages)
        if (pageable.getPageNumber() == 0) {
            int pageSize = pageable.getPageSize();
            boolean isWarmed = false;
            try {
                isWarmed = messageCache.isWarmed(chatroomId);
            } catch (Exception e) {
                log.debug("[Retrieval] Failed to check isWarmed for room {}: {}", chatroomId, e.getMessage());
            }

            if (isWarmed) {
                List<ChatMessageRedisDTO> cached = messageCache.getHistory(chatroomId,
                        pageSize);

                if (cached != null) {
                    log.debug("[Retrieval] Cache HIT (Warmed) for room {}. Returning {} messages.", chatroomId,
                            cached.size());
                    return cached.stream()
                            .map(ChatMessageRedisDTO::toEntity)
                            .toList();
                }
                log.warn(
                        "[Retrieval] Cache availability check failed (CB Open/Error) for room {}. Proceeding to DB fallback.",
                        chatroomId);
            }
            log.info("[Retrieval] Cache MISS (Unwarmed) for room {}. Falling back to MySQL.", chatroomId);
        }

        // Fallback or deeper pages: MySQL
        List<ChatMessageEntity> dbResults = fetchFromDatabase(chatroomId, pageable);

        // Optimization: Write-back to Redis for Page 0 (fire-and-forget)
        if (pageable.getPageNumber() == 0 && !dbResults.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("[Retrieval] Warming up Redis cache for room {} with {} messages.", chatroomId,
                            dbResults.size());
                    List<ChatMessageRedisDTO> dtos = dbResults.stream()
                            .map(ChatMessageRedisDTO::fromEntity)
                            .toList();
                    messageCache.warmUpHistory(chatroomId, dtos);
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
    Optional<ChatMessageEntity> findById(Long messageId) {
        return messageRepository.findById(messageId);
    }

    @Transactional(readOnly = true)
    List<ChatMessageEntity> findAllById(Iterable<Long> messageIds) {
        return messageRepository.findAllById(messageIds);
    }

    /**
     * Search messages using Full-Text Search.
     * Direct DB access, no caching.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> searchMessage(Integer chatroomId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return messageRepository.searchByMessage(chatroomId, keyword);
    }

    /**
     * Calculate which page a message belongs to.
     * Returns 0-based page index.
     */
    @Transactional(readOnly = true)
    public int getMessagePage(Integer chatroomId, Long messageId, int pageSize) {
        return messageRepository.findById(messageId)
                .map(msg -> {
                    long countAfter = messageRepository.countByChatroomIdAndChatTimeAfter(chatroomId,
                            msg.getChatTime());
                    return (int) (countAfter / pageSize);
                })
                .orElse(0); // Default to first page if not found
    }
}
