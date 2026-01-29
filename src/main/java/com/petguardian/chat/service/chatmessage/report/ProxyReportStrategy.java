package com.petguardian.chat.service.chatmessage.report;

import com.petguardian.chat.model.ChatReport;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceCircuitBreaker;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Proxy Strategy for Chat Reports.
 * Implements "Cache-Aside" Pattern with Circuit Breaker Resilience.
 * 
 * Logic:
 * - Write: MySQL -> Invalidate Redis
 * - Read: Redis (Hit) OR MySQL -> Repopulate Redis (Miss)
 */
@Service
@Primary
public class ProxyReportStrategy implements ReportStrategyService {

    private static final Logger log = LoggerFactory.getLogger(ProxyReportStrategy.class);

    private final MysqlReportStrategy mysqlStrategy;
    private final RedisReportStrategy redisStrategy;
    private final ResilienceCircuitBreaker circuitBreaker;

    public ProxyReportStrategy(
            MysqlReportStrategy mysqlStrategy,
            RedisReportStrategy redisStrategy,
            ObjectProvider<ResilienceCircuitBreaker> circuitProvider) {
        this.mysqlStrategy = mysqlStrategy;
        this.redisStrategy = redisStrategy;

        // Reuse existing Circuit Breaker implementation
        this.circuitBreaker = circuitProvider.getObject();
        this.circuitBreaker.setName("Report-Redis-Circuit");
    }

    @Override
    public void save(ChatReport report) {
        // 1. Write to System of Record (MySQL) - Blocking/Critical
        mysqlStrategy.save(report);

        // 2. Invalidate Cache - Resilient/Non-Blocking
        if (circuitBreaker.isOpen()) {
            log.warn("[Report] Circuit Open. Skipping Cache Invalidation. Cache may be stale (TTL will expire).");
            return;
        }

        try {
            redisStrategy.save(report); // Actually executes invalidate()
            circuitBreaker.recordSuccess();
        } catch (Exception e) {
            log.error("[Report] Redis Invalidation Failed: {}", e.getMessage());
            circuitBreaker.recordFailure(e);
            // We do NOT throw exception here. The Report is saved in MySQL, which is what
            // matters.
        }
    }

    @Override
    public Map<String, Integer> getBatchStatus(Integer reporterId, List<String> messageIds) {
        // 1. Try Cache (Zero SQL)
        if (!circuitBreaker.isOpen()) {
            try {
                Map<String, Integer> cached = redisStrategy.getBatchStatus(reporterId, messageIds);
                if (cached != null) {
                    circuitBreaker.recordSuccess();
                    return cached;
                }
                // Cache Miss (Result is null) -> Proceed to Lazy Load
            } catch (Exception e) {
                log.warn("[Report] Redis Read Failed: {}", e.getMessage());
                circuitBreaker.recordFailure(e);
                // Fallthrough to MySQL
            }
        }

        // 2. Fallback / Lazy Load (MySQL)
        log.debug("[Report] Cache Miss/Failure for User {}. Loading from MySQL.", reporterId);

        // Fetch requested statuses (for return)
        Map<String, Integer> result = mysqlStrategy.getBatchStatus(reporterId, messageIds);

        // 3. Cache Repopulation (Async-ish or Sync?)
        // We typically do this synchronously in standard Cache-Aside.
        // We fetch ALL reports for this user to simplify the cache structure (User ->
        // All Reports Hash).
        if (!circuitBreaker.isOpen()) {
            try {
                // Fetch FULL state for warming
                Map<String, Integer> fullState = mysqlStrategy.getAllByUser(reporterId);
                redisStrategy.repopulate(reporterId, fullState);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                log.error("[Report] Cache Repopulation Failed: {}", e.getMessage());
                circuitBreaker.recordFailure(e);
            }
        }

        return result;
    }
}
