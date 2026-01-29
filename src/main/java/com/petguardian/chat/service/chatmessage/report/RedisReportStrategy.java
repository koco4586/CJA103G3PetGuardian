package com.petguardian.chat.service.chatmessage.report;

import com.petguardian.chat.model.ChatReport;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis Implementation of Report Strategy.
 * Acts as the Cache Sidecar (Write-Invalidate, Read-Repopulate).
 */
@Service
public class RedisReportStrategy implements ReportStrategyService {

    private static final String KEY_PREFIX = "chat:reports:";
    private static final long TTL_DAYS = 7;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisReportStrategy(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getKey(Integer reporterId) {
        return KEY_PREFIX + reporterId;
    }

    /**
     * Invalidate Cache (Delete Key).
     * Used after any write operation to MySQL.
     */
    public void invalidate(Integer reporterId) {
        redisTemplate.delete(getKey(reporterId));
    }

    /**
     * Repopulate Cache.
     * Used on Cache Miss (Lazy Loading).
     */
    public void repopulate(Integer reporterId, Map<String, Integer> statusMap) {
        String key = getKey(reporterId);

        // Even if Map is empty, we MUST create the Key with a marker to prevent Cache
        // Penetration
        // (i.e. to stop hitting MySQL repeatedly for users with 0 reports)
        if (statusMap != null && !statusMap.isEmpty()) {
            redisTemplate.opsForHash().putAll(key, statusMap);
        }

        // Always add init marker
        redisTemplate.opsForHash().put(key, "_init", 1);
        redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS);
    }

    /**
     * Batch Get Status.
     * Returns NULL if Key doesn't exist (Cache Miss).
     * Returns Map (potentially empty) if Key exists (Cache Hit).
     */
    @Override
    public Map<String, Integer> getBatchStatus(Integer reporterId, List<String> messageIds) {
        String key = getKey(reporterId);

        // Check if key exists (Cache Hit/Miss check)
        // We use 'hasKey' or just try to get.
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return null; // Cache Miss (Not initialized or Expired/Deleted)
        }

        if (messageIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // MultiGet from Hash
        // Generics hell with RedisTemplate<String, Object>.
        // We cast inputs to Object and results to Integer.
        List<Object> hashKeys = messageIds.stream().collect(Collectors.toList());
        List<Object> results = redisTemplate.opsForHash().multiGet(key, hashKeys);

        // Map results back to MessageIDs
        // Note: multiGet returns non-null List, but elements can be null (not found in
        // hash)
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        for (int i = 0; i < messageIds.size(); i++) {
            Object val = results.get(i);
            if (val instanceof Integer) {
                map.put(messageIds.get(i), (Integer) val);
            }
            // If null, it means "Not Reported" (assuming we cached ALL reports for user)
        }

        return map;
    }

    @Override
    public void save(ChatReport report) {
        // Method from interface, but for Redis Strategy (Cache-Aside),
        // we invalidate here instead of "Save".
        invalidate(report.getReporterId());
    }
}
