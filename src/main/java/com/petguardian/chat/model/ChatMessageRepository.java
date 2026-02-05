package com.petguardian.chat.model;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    /**
     * Find all messages in a chatroom, ordered by time ascending
     */
    List<ChatMessageEntity> findByChatroomIdOrderByChatTimeAsc(Integer chatroomId);

    /**
     * Find the latest message in a chatroom.
     * Uses a short name as requested to avoid over-complexity.
     */
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.chatroomId = :chatroomId ORDER BY m.chatTime DESC")
    List<ChatMessageEntity> findLatest(Integer chatroomId, Pageable pageable);

    /**
     * Full-Text Search using MySQL ngram parser.
     * Native Query required for MATCH() AGAINST().
     */
    List<ChatMessageEntity> searchByMessage(Integer chatroomId, String keyword);

    /**
     * Count messages after a specific time (for page calculation).
     */
    long countByChatroomIdAndChatTimeAfter(Integer chatroomId, java.time.LocalDateTime chatTime);
}
