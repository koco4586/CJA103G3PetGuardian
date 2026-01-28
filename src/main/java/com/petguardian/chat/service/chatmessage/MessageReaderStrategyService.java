package com.petguardian.chat.service.chatmessage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.petguardian.chat.model.ChatMessageEntity;

/**
 * Strategy Interface for Message Reading Operations.
 * 
 * Segregated from the main strategy to allow consumers to strictly depend on
 * read capabilities.
 */
public interface MessageReaderStrategyService {

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
