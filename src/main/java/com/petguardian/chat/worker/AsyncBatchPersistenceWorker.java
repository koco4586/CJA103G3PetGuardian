package com.petguardian.chat.worker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;

import com.petguardian.chat.service.chatmessage.MessageBufferService;
import com.petguardian.chat.service.chatmessage.MessageCreationContext;
import com.petguardian.chat.service.chatmessage.MessageWriterStrategyService;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceCircuitBreaker;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceFlowControl;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceFlushManager;

/**
 * Async Worker bridging the Buffer Source and Storage Target.
 * 
 * - Multi-Threaded Consumer (One per Shard).
 * - Delegates Batch Processing Logic to {@link ResilienceFlushManager}.
 * - Implements High-Availability Loop.
 */
public class AsyncBatchPersistenceWorker implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AsyncBatchPersistenceWorker.class);

    private static final int WORKER_CONCURRENCY = 10; // Match Shard Count
    private static final int BATCH_SIZE = 500;
    private static final long POLL_TIMEOUT_MS = 1000; // Increased to 1000ms for more stable polling
    private static final long LOG_THROTTLE_MS = 60000; // Log connection error only once per minute

    private final String workerName;
    private final MessageBufferService source;
    private final MessageWriterStrategyService target; // Target Storage (Writer)
    private final ResilienceFlushManager flushManager;
    private final ResilienceFlowControl flowControl;
    private final ResilienceCircuitBreaker circuitBreaker;
    private final ResilienceChatFailureHandler failureHandler;

    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicLong lastConnectionErrorLog = new AtomicLong(0);

    public AsyncBatchPersistenceWorker(
            String workerName,
            MessageBufferService source,
            MessageWriterStrategyService target,
            ResilienceFlushManager flushManager,
            ResilienceFlowControl flowControl,
            ObjectProvider<ResilienceCircuitBreaker> circuitProvider,
            ResilienceChatFailureHandler failureHandler) {

        this.workerName = workerName;
        this.source = source;
        this.target = target;
        this.flushManager = flushManager;
        this.flowControl = flowControl;
        this.circuitBreaker = circuitProvider.getObject();
        this.failureHandler = failureHandler;
        this.circuitBreaker.setName(workerName + "-Circuit");

        this.executor = Executors.newFixedThreadPool(WORKER_CONCURRENCY, r -> {
            Thread t = new Thread(r);
            t.setName("ChatW-" + workerName + "-" + t.getId());
            return t;
        });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("[{}] Starting with {} threads...", workerName, WORKER_CONCURRENCY);
        for (int i = 0; i < WORKER_CONCURRENCY; i++) {
            int shardId = i;
            executor.submit(() -> runShardConsumer(shardId));
        }
    }

    private void runShardConsumer(int shardId) {
        log.info("[{}] Worker-Thread-{} IDENTITY started. Target Class: {}",
                workerName, Thread.currentThread().getName(), target.getClass().getSimpleName());

        while (running.get()) {
            try {
                // 1.Flow Control (Backpressure)
                // Checks health of both Source (where we read) and Target (where we write)
                if (flowControl.shouldPause(circuitBreaker, () -> source.isHealthy() && target.isHealthy())) {
                    // Backoff to avoid spinning while target is down
                    try {
                        Thread.sleep(ResilienceFlowControl.BACKOFF_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }

                // 2. Poll Batch (Blocking with Timeout)
                List<MessageCreationContext> batch = source.pollBatch(shardId, BATCH_SIZE, POLL_TIMEOUT_MS);

                if (batch.isEmpty()) {
                    // Success: Connection works even if no data. Reset circuit or release probe.
                    circuitBreaker.recordSuccess();
                    continue; // Idle (0 messages in 1s)
                }

                // 3. Process Batch via Resilience Manager
                // Note: On retry, we push back to Redis (Right side) using source.requeue()
                flushManager.processBatch(
                        batch,
                        target,
                        circuitBreaker,
                        (failedBatch) -> source.requeue(shardId, failedBatch));

            } catch (Exception e) {
                if (failureHandler.isConnectionException(e)) {
                    long now = System.currentTimeMillis();
                    long lastLog = lastConnectionErrorLog.get();
                    if (now - lastLog > LOG_THROTTLE_MS) {
                        if (lastConnectionErrorLog.compareAndSet(lastLog, now)) {
                            // Suppress -> Only log once per minute for all threads
                            log.warn(
                                    "[{}] Connection Error detected by Worker-{}: {}. Suppressing further logs for {}s.",
                                    workerName, shardId, e.getMessage(), LOG_THROTTLE_MS / 1000);
                        }
                    }
                    // Record failure in circuit breaker to trigger pause if source or pipeline is
                    // broken
                    circuitBreaker.recordFailure(e);
                } else {
                    log.error("Worker-{} Loop Error: {}", shardId, e.getMessage());
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        }

    }

    @Override
    public void destroy() throws Exception {
        log.info("[{}] Stopping...", workerName);
        running.set(false);
        executor.shutdown();

        // 1. Wait for active workers to finish
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            log.warn("Forcing shutdown of worker threads...");
            executor.shutdownNow();
        }

        // 2. Final Flush of Remaining Messages (Graceful Shutdown)
        log.info("Starting Graceful Shutdown Flush for {} Shards...", WORKER_CONCURRENCY);
        int totalFlushed = 0;

        for (int i = 0; i < WORKER_CONCURRENCY; i++) {
            int shardId = i;
            // Poll with very short timeout (1ms) to drain queue without blocking
            // We loop until queue is empty for this shard
            while (true) {
                try {
                    List<MessageCreationContext> batch = source.pollBatch(shardId, BATCH_SIZE, 1);
                    if (batch.isEmpty()) {
                        break; // Shard Empty
                    }

                    log.info("Flushing {} messages from Shard-{} during shutdown...", batch.size(), shardId);

                    // Process synchronously on the main thread
                    int saved = flushManager.processBatch(
                            batch,
                            target,
                            circuitBreaker,
                            (failedBatch) -> {
                                log.error("Failed to flush batch of {} during shutdown. Data may remain in Redis.",
                                        failedBatch.size());
                                // Try one last save.
                            });
                    totalFlushed += saved;

                } catch (Exception e) {
                    log.error("Error flushing Shard-{} during shutdown: {}", shardId, e.getMessage());
                    break;
                }
            }
        }

        log.info("[{}] Stopped. Total flushed during shutdown: {}", workerName, totalFlushed);
    }
}
