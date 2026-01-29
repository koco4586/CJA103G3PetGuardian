package com.petguardian.chat.service.chatmessage.report;

import com.petguardian.chat.model.ChatReport;
import com.petguardian.chat.model.ChatReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MySQL Implementation of Report Strategy.
 * Acts as the System of Record.
 */
@Service
public class MysqlReportStrategy implements ReportStrategyService {

    private final ChatReportRepository repository;

    public MysqlReportStrategy(ChatReportRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void save(ChatReport report) {
        repository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getBatchStatus(Integer reporterId, List<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ChatReport> reports = repository.findByReporterIdAndMessageIdIn(reporterId, messageIds);
        return reports.stream()
                .collect(Collectors.toMap(ChatReport::getMessageId, ChatReport::getReportStatus));
    }

    /**
     * Special method for Cache Warming logic.
     * Fetches ALL reports for a user to repopulate Redis on a miss.
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getAllByUser(Integer reporterId) {
        List<ChatReport> allReports = repository.findByReporterId(reporterId);
        return allReports.stream()
                .collect(Collectors.toMap(ChatReport::getMessageId, ChatReport::getReportStatus));
    }
}
