package com.petguardian.chat.service.chatmessage;

/**
 * Strategy Interface for Resilience Support.
 * 
 * Provides health checks and exception classification for Circuit Breakers
 * and Retry mechanisms.
 */
public interface ResilienceSupportStrategyService {

    /**
     * Checks the health of the underlying storage.
     * Used by the Proxy ensuring availability before switching strategies.
     * 
     * @return true if healthy/available
     */
    default boolean isHealthy() {
        return true;
    }

    /**
     * Checks if the exception represents a connectivity issue.
     * Used by Circuit Breakers to decide whether to trip.
     */
    default boolean isConnectionException(Throwable e) {
        return false;
    }

    /**
     * Checks if the exception represents a data integrity violation.
     * Used by FallbackHandler to identify Poison Messages.
     */
    default boolean isDataIntegrityViolation(Throwable e) {
        return false;
    }
}
