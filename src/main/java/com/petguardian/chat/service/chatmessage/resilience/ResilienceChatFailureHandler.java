package com.petguardian.chat.service.chatmessage.resilience;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisCommandTimeoutException;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.sql.SQLTransientConnectionException;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Centralized strategy for classifying exceptions.
 * Replaces duplicate logic in MysqlMessageStrategyServiceImpl and
 * RedisMessageStrategyServiceImpl.
 * Used by Resilience components to determine fallback behavior.
 */
@Component
public class ResilienceChatFailureHandler {

    // ============================================================
    // CONNECTION / TRANSIENT ERRORS (Retryable)
    // ============================================================

    /**
     * Determines if the exception represents a temporary connection failure.
     * These errors typically warrant a retry or circuit breaker trip.
     */
    public boolean isConnectionException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            String name = cause.getClass().getName();

            // 1. Check Strict Types (Strong Coupling but Safe)
            if (isTypedConnectionError(cause)) {
                return true;
            }

            // 2. Check Name Patterns (Soft Coupling for Resilience)
            if (isNameHeuristicMatch(name)) {
                return true;
            }

            cause = cause.getCause();
        }
        return false;
    }

    private boolean isTypedConnectionError(Throwable cause) {
        return cause instanceof SQLTransientConnectionException ||
                cause instanceof DataAccessResourceFailureException ||
                cause instanceof CannotCreateTransactionException ||
                cause instanceof SocketTimeoutException ||
                cause instanceof ConnectException ||
                cause instanceof QueryTimeoutException ||
                cause instanceof RedisConnectionException ||
                cause instanceof RedisCommandTimeoutException ||
                cause instanceof RedisConnectionFailureException;
    }

    private boolean isNameHeuristicMatch(String name) {
        return name.contains("CommunicationsException") ||
                name.contains("SocketTimeout") ||
                name.contains("ConnectException") ||
                name.contains("JDBCConnectionException") ||
                name.contains("QueryTimeoutException") ||
                name.contains("RedisSystemException");
    }

    // ============================================================
    // DATA INTEGRITY / PERMANENT ERRORS (Non-Retryable)
    // ============================================================

    /**
     * Determines if the exception represents a permanent data integrity violation.
     * These errors should NOT be retried and may require "Poison Pill" isolation.
     */
    public boolean isDataIntegrityViolation(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof DataIntegrityViolationException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
