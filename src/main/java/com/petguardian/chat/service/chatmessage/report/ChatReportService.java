package com.petguardian.chat.service.chatmessage.report;

import com.petguardian.chat.model.ChatReport;
import com.petguardian.chat.model.ChatReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatReportService {

    private final ChatReportRepository chatReportRepository;
    private final ReportStrategyService reportStrategy;

    public ChatReportService(ChatReportRepository chatReportRepository, ReportStrategyService reportStrategy) {
        this.chatReportRepository = chatReportRepository;
        this.reportStrategy = reportStrategy;
    }

    /**
     * Submit a new report.
     * Enforces unique constraint check.
     */
    @Transactional
    public void submitReport(Integer reporterId, String messageId, int type, String reason) {
        if (chatReportRepository.existsByReporterIdAndMessageId(reporterId, messageId)) {
            throw new IllegalStateException("You have already reported this message.");
        }

        ChatReport report = new ChatReport();
        report.setReporterId(reporterId);
        report.setMessageId(messageId);
        report.setReportType(type);
        report.setReportReason(reason);
        report.setReportStatus(1); // Default to Pending (1)

        // Use Strategy for Save (Triggers Cache Invalidation)
        reportStrategy.save(report);
    }

    /**
     * Get report status for a specific message and user.
     * Uses Strategy for potential Cache Hit (Zero SQL).
     */
    public Integer getReportStatus(Integer reporterId, String messageId) {
        Map<String, Integer> statusMap = reportStrategy.getBatchStatus(reporterId,
                Collections.singletonList(messageId));
        return statusMap.getOrDefault(messageId, 0); // 0: Not Reported
    }

    public List<ChatReport> getPendingReports() {
        return chatReportRepository.findByReportStatus(1);
    }

    public List<ChatReport> getClosedReports() {
        return chatReportRepository.findByReportStatusIn(Arrays.asList(2, 3));
    }

    @Transactional
    public void updateReportStatus(Integer reportId, int status, Integer handlerId, String note) {
        Optional<ChatReport> reportOpt = chatReportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            ChatReport report = reportOpt.get();
            report.setReportStatus(status);
            report.setHandlerId(handlerId);
            report.setHandleTime(LocalDateTime.now());
            if (note != null) {
                report.setHandleNote(note);
            }
            // Use Strategy to Save (Triggers Cache Invalidation for the Reporter)
            reportStrategy.save(report);
        } else {
            throw new IllegalArgumentException("Report not found: " + reportId);
        }
    }
}
