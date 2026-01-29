package com.petguardian.chat.service.chatmessage.report;

import com.petguardian.chat.model.ChatReport;

import java.util.List;
import java.util.Map;

/**
 * Strategy Interface for Chat Report Persistence and Retrieval.
 * Supports Cache-Aside pattern (Redis + MySQL).
 */
public interface ReportStrategyService {

    /**
     * Persist a new report.
     * Implementation should handle Invalidating the Cache.
     */
    void save(ChatReport report);

    /**
     * Batch retrieve report statuses for specific messages.
     * Used for efficient "Zero SQL" reads in chat history.
     */
    Map<String, Integer> getBatchStatus(Integer reporterId, List<String> messageIds);
}
