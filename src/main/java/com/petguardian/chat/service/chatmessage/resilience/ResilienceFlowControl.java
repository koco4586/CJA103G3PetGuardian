package com.petguardian.chat.service.chatmessage.resilience;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

/**
 * Generic Flow Control Component.
 */
@Component
public class ResilienceFlowControl {

    public static final long BACKOFF_MS = 5000;

    /**
     * determining if the execution flow should be paused.
     * 
     * @param circuit     The circuit breaker guarding the target resource.
     * @param healthProbe Functional interface to check if target is healthy.
     * @return true if the caller should pause/skip execution (Target is down).
     *         false if the caller can proceed (Circuit Closed or Recovered).
     */
    public boolean shouldPause(ResilienceCircuitBreaker circuit, Supplier<Boolean> healthProbe) {
        if (circuit.isOpen()) {
            // Circuit is OPEN or in COOLDOWN.
            // Under the new Half-Open model, isOpen() returns false after the
            // recovery timeout has passed. So if it's still true, we must pause.
            return true;
        }

        // Circuit is CLOSED or HALF-OPEN (recovery timeout passed).
        // Let the actual operation proceed; its success/failure will fully reset
        // or re-trip the circuit.
        return false;
    }
}
