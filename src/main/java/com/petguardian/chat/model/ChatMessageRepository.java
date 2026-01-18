package com.petguardian.chat.model;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessageVO, String> {

    /**
     * Find all messages in a chatroom, ordered by time ascending
     */
    List<ChatMessageVO> findByChatroomIdOrderByChatTimeAsc(Integer chatroomId);

    /**
     * Find the latest message in a chatroom.
     * Uses a short name as requested to avoid over-complexity.
     */
    @Query("SELECT m FROM ChatMessageVO m WHERE m.chatroomId = :chatroomId ORDER BY m.chatTime DESC")
    List<ChatMessageVO> findLatest(Integer chatroomId, Pageable pageable);
}
