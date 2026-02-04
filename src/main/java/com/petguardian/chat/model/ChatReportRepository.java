package com.petguardian.chat.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatReportRepository extends JpaRepository<ChatReport, Integer> {

    /**
     * Check if a report exists for a specific user and message.
     * Used to prevent duplicate reports.
     */
    boolean existsByReporterIdAndMessageId(Integer reporterId, String messageId);

    /**
     * Find report status for a user and message.
     * Used by the API to return current status to frontend.
     */
    Optional<ChatReport> findByReporterIdAndMessageId(Integer reporterId, String messageId);

    /**
     * Find reports by status (e.g., 0 for pending).
     */
    List<ChatReport> findByReportStatus(Integer status);

    /**
     * Find reports by status IN list (e.g., Closed or Rejected).
     */
    List<ChatReport> findByReportStatusIn(Collection<Integer> statuses);

    /**
     * Cache Warming: Find all reports by a specific user.
     */
    List<ChatReport> findByReporterId(Integer reporterId);

    /**
     * Batch Optimization: Find reports for a specific user and list of messages.
     */
    List<ChatReport> findByReporterIdAndMessageIdIn(Integer reporterId, Collection<String> messageIds);

    // 後台dashboard統計用：計算指定檢舉狀態的檢舉單數量
    long countByReportStatus(Integer returnStatus);

}
