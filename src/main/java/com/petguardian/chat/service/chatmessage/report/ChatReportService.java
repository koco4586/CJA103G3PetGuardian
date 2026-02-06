package com.petguardian.chat.service.chatmessage.report;

import com.petguardian.chat.model.ChatReport;
import com.petguardian.chat.model.ChatReportRepository;
import com.petguardian.chat.service.RedisJsonMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing Chat Reports.
 * Consolidates business logic, persistence, and caching strategies.
 * Implements Cache-Aside (Redis + MySQL) with Circuit Breaker resilience.
 */
@Service
public class ChatReportService {

    private static final Logger log = LoggerFactory.getLogger(ChatReportService.class);
    private static final String REDIS_KEY_PREFIX = "chat:report_status:";
    private static final long TTL_DAYS = 7;
    private static final String CIRCUIT_NAME = "reportCircuit";

    private final ChatReportRepository chatReportRepository;
    private final RedisJsonMapper redisJsonMapper;
    private final CircuitBreaker circuitBreaker;

    public ChatReportService(
            ChatReportRepository chatReportRepository,
            RedisJsonMapper redisJsonMapper,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.chatReportRepository = chatReportRepository;
        this.redisJsonMapper = redisJsonMapper;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_NAME);
    }

    /**
     * Batch retrieves report status for multiple messages.
     * Uses HMGET to fetch only requested fields.
     */
    public Map<Long, Integer> getBatchStatus(Integer reporterId, List<Long> messageIds) {
        if (reporterId == null || messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. Try Cache
        try {
            Map<Long, Integer> cached = circuitBreaker.executeSupplier(() -> getFromRedis(reporterId, messageIds));
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.debug("[Report] Redis Cache Unavailable: {}", e.getMessage());
        }

        // 2. Cache Miss: Fetch FULL state for this user to simplify cache structure
        try {
            Map<Long, Integer> fullState = getAllFromMysql(reporterId);

            // Repopulate Cache (Functional API)
            try {
                circuitBreaker.executeRunnable(() -> repopulateRedis(reporterId, fullState));
            } catch (Exception e) {
                log.debug("[Report] Cache Repopulation Failed: {}", e.getMessage());
            }

            // Filter results for requested messageIds
            Map<Long, Integer> results = new HashMap<>();
            for (Long mid : messageIds) {
                if (fullState.containsKey(mid)) {
                    results.put(mid, fullState.get(mid));
                }
            }
            return results;
        } catch (Exception e) {
            log.error("[Report] Cold Start Failed: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Transactional
    public void submitReport(Integer reporterId, String messageId, int type, String reason) {
        Long messageIdLong = io.hypersistence.tsid.TSID.from(messageId).toLong();
        if (chatReportRepository.existsByReporterIdAndMessageId(reporterId, messageIdLong)) {
            throw new IllegalStateException("Message already reported by this user.");
        }

        ChatReport report = new ChatReport();
        report.setReporterId(reporterId);
        report.setMessageId(messageIdLong);
        report.setReportType(type);
        report.setReportReason(reason);
        report.setReportStatus(0); // 0: Pending (待處理)
        report.setReportTime(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        chatReportRepository.save(report);

        // Invalidate Cache
        safeInvalidateCache(reporterId);
    }

    public List<ChatReport> getPendingReports() {
        return chatReportRepository.findByReportStatus(0); // 0: Pending
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

            chatReportRepository.save(report);

            // Invalidate Cache
            safeInvalidateCache(report.getReporterId());
        } else {
            throw new IllegalArgumentException("Report not found: " + reportId);
        }
    }

    // =================================================================================
    // Internal Helper Methods
    // =================================================================================

    private String getRedisKey(Integer reporterId) {
        return REDIS_KEY_PREFIX + reporterId;
    }

    public Map<Long, Integer> getFromRedis(Integer reporterId, List<Long> messageIds) {
        String key = getRedisKey(reporterId);

        List<Object> hashKeys = new ArrayList<>(messageIds.size() + 1);
        messageIds.forEach(id -> hashKeys.add(io.hypersistence.tsid.TSID.from(id).toString()));
        hashKeys.add("_init");

        // Use MultiGet via String Template (returns Strings)
        // Note: GenericJackson2JsonRedisSerializer might have been used before.
        // If we switched to StringRedisTemplate, multiGet returns List<String>.
        // We cast to List<Object> here just to iterate, but values are Strings.
        List<Object> values = redisJsonMapper.getStringTemplate().opsForHash().multiGet(key, hashKeys);

        if (values == null || values.size() != hashKeys.size()) {
            return null;
        }

        Object initMarker = values.get(values.size() - 1);
        if (initMarker == null) {
            return null;
        }

        Map<Long, Integer> result = new HashMap<>();
        for (int i = 0; i < messageIds.size(); i++) {
            Object val = values.get(i);
            if (val != null) {
                try {
                    // It should be a string now (from StringRedisTemplate)
                    String strVal = val.toString();
                    result.put(messageIds.get(i), Integer.parseInt(strVal));
                } catch (NumberFormatException e) {
                    log.warn("[Report] Failed to parse Redis value: {}", val);
                }
            }
        }
        return result;
    }

    private Map<Long, Integer> getAllFromMysql(Integer reporterId) {
        try {
            List<ChatReport> reports = chatReportRepository.findByReporterId(reporterId);
            return reports.stream()
                    .collect(Collectors.toMap(ChatReport::getMessageId, ChatReport::getReportStatus, (v1, v2) -> v1));
        } catch (Exception e) {
            log.error("[Report] MySQL Data Load Failed for User {}: {}", reporterId, e.getMessage());
            return Collections.emptyMap();
        }
    }

    private void safeInvalidateCache(Integer reporterId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        circuitBreaker.executeRunnable(() -> performInvalidation(reporterId));
                    } catch (Exception e) {
                        log.warn("[Report] Invalidation Failed: {}", e.getMessage());
                        // Note: Throwing here might not roll back committed TX, but logs error.
                    }
                }
            });
        } else {
            try {
                circuitBreaker.executeRunnable(() -> performInvalidation(reporterId));
            } catch (Exception e) {
                log.warn("[Report] Invalidation Failed: {}", e.getMessage());
                throw new RuntimeException("Cache invalidation failed", e);
            }
        }
    }

    public void performInvalidation(Integer reporterId) {
        redisJsonMapper.delete(getRedisKey(reporterId));
    }

    public void repopulateRedis(Integer reporterId, Map<Long, Integer> statusMap) {
        String key = getRedisKey(reporterId);
        // We need Map<String, String> for StringRedisTemplate
        Map<String, String> writeMap = new HashMap<>();
        if (statusMap != null) {
            statusMap.forEach((k, v) -> writeMap.put(io.hypersistence.tsid.TSID.from(k).toString(), String.valueOf(v)));
        }
        writeMap.put("_init", "1");

        redisJsonMapper.getStringTemplate().opsForHash().putAll(key, writeMap);
        redisJsonMapper.expire(key, java.time.Duration.ofDays(TTL_DAYS));
    }
}
