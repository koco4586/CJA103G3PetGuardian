package com.petguardian.chat.service.chatmessage.resilience;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Thread-Safe Circuit Breaker Implementation.
 * 
 * - Uses Atomic variables for wait-free concurrency.
 * - Tracks consecutive failures.
 * - Manages OPEN/CLOSED state.
 * 
 * Designed to be instantiated per-resource (Primary/Secondary).
 * It is a Prototype Component so we can inject instances.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ResilienceCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(ResilienceCircuitBreaker.class);
    private static final int FAILURE_THRESHOLD = 5;
    private static final long RECOVERY_TIMEOUT_MS = 30000; // 30 seconds recovery window

    private String name = "Default";
    private final AtomicBoolean open = new AtomicBoolean(false);
    private final AtomicBoolean probing = new AtomicBoolean(false); // Lock for Half-Open probe
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);

    public ResilienceCircuitBreaker() {
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Checks if the circuit is open.
     * Incorporates Half-Open logic: if the circuit has been open longer than
     * the recovery timeout, it allows one trial request (returns false).
     */
    public boolean isOpen() {
        if (!open.get()) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - lastFailureTime.get() > RECOVERY_TIMEOUT_MS) {
            // Attempt to take the probe lock (Thread-Safe Half-Open)
            if (probing.compareAndSet(false, true)) {
                log.info("[{}] Circuit Recovery Cooldown finished. Testing ONE probe...", name);
                return false; // This winner thread will verify the target
            }
        }
        return true; // Others continue to fail-fast
    }

    public void trip() {
        if (open.compareAndSet(false, true)) {
            lastFailureTime.set(System.currentTimeMillis());
            probing.set(false); // Release any potential probe lock
            log.error("[{}] Circuit Breaker TRIPPED. System entering fallback/wait state.", name);
        }
    }

    /**
     * Bypasses the failure threshold and trips the circuit immediately.
     * Used for definitive connection failures (e.g. Connection Refused).
     */
    public void tripImmediately() {
        log.warn("[{}] DEFINITIVE FAILURE DETECTED. Tripping circuit immediately.", name);
        consecutiveFailures.set(FAILURE_THRESHOLD);
        lastFailureTime.set(System.currentTimeMillis());
        trip();
    }

    public void reset() {
        if (open.compareAndSet(true, false)) {
            consecutiveFailures.set(0);
            probing.set(false); // Ensure lock is released
            log.info("[{}] Circuit Breaker RESET. System resuming normal operations.", name);
        }
    }

    /**
     * Records a successful operation.
     * If the circuit was open (Half-Open probe success), resets the circuit.
     */
    public void recordSuccess() {
        if (open.get()) {
            reset();
        } else {
            consecutiveFailures.set(0);
            probing.set(false); // Safety release
        }
    }

    private static final long MIN_FAILURE_INTERVAL_MS = 50; // Reduced for faster detection

    public void recordFailure(Throwable e) {
        long now = System.currentTimeMillis();
        long last = lastFailureTime.get();

        // 1. Thread-Safe Debounce check
        if (now - last < MIN_FAILURE_INTERVAL_MS) {
            return;
        }

        // 2. Atomic Update of lastFailureTime
        if (lastFailureTime.compareAndSet(last, now)) {
            probing.set(false); // Release probe lock to allow another trial after next cooldown
            int count = consecutiveFailures.incrementAndGet();
            log.warn("[{}] Circuit Persistence Failure (Count: {}/{}). Error: {}", name, count, FAILURE_THRESHOLD,
                    e.getMessage());

            if (count >= FAILURE_THRESHOLD) {
                trip();
            }
        } else {
            // If we didn't update (debounced), we should still release the probe lock if we
            // took it
            probing.compareAndSet(true, false);
        }
    }
}
