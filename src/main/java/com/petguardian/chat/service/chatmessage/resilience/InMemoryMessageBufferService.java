package com.petguardian.chat.service.chatmessage.resilience;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.petguardian.chat.service.chatmessage.MessageBufferService;
import com.petguardian.chat.service.chatmessage.MessageCreationContext;

/**
 * In-Memory Implementation of Message Buffer.
 * 
 * - Uses ConcurrentLinkedDeque for high-throughput buffering.
 * - Implements strict Backpressure control.
 * - Single Shard (0) configuration.
 */
@Service("inMemoryBuffer")
public class InMemoryMessageBufferService implements MessageBufferService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryMessageBufferService.class);
    private static final int MAX_BUFFER_SIZE = 10000;

    private final ConcurrentLinkedDeque<MessageCreationContext> buffer = new ConcurrentLinkedDeque<>();
    private final AtomicInteger bufferSize = new AtomicInteger(0);

    @Override
    public void offer(int shardId, MessageCreationContext context) {
        // Reservation Pattern
        int current = bufferSize.incrementAndGet();
        if (current > MAX_BUFFER_SIZE) {
            bufferSize.decrementAndGet(); // Rollback
            log.error("BACKPRESSURE: Buffer Full ({}/{}). Rejecting request.", current - 1, MAX_BUFFER_SIZE);
            throw new RuntimeException("Service Overloaded: Chat System is temporarily unavailable.");
        }

        try {
            buffer.offer(context);
        } catch (Exception e) {
            bufferSize.decrementAndGet();
            throw e;
        }
    }

    @Override
    public List<MessageCreationContext> pollBatch(int shardId, int batchSize, long timeoutMs) {
        List<MessageCreationContext> batch = new ArrayList<>();

        // 1. Try immediate poll
        drainBatch(batch, batchSize);

        // 2. If empty and timeout provided, wait and try again
        if (batch.isEmpty() && timeoutMs > 0) {
            try {
                // Determine wait slice to avoid over-sleeping newly arrived messages
                long slept = 0;
                long interval = 100; // Check every 100ms
                while (slept < timeoutMs) {
                    Thread.sleep(Math.min(interval, timeoutMs - slept));
                    slept += interval;

                    drainBatch(batch, batchSize);
                    if (!batch.isEmpty()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return batch;
    }

    private void drainBatch(List<MessageCreationContext> batch, int batchSize) {
        if (bufferSize.get() == 0)
            return;

        MessageCreationContext ctx;
        while (batch.size() < batchSize && (ctx = buffer.poll()) != null) {
            batch.add(ctx);
            bufferSize.decrementAndGet();
        }
    }

    @Override
    public long getQueueDepth(int shardId) {
        return bufferSize.get();
    }

    @Override
    public void requeue(int shardId, List<MessageCreationContext> batch) {
        if (batch == null || batch.isEmpty())
            return;

        // Add in reverse order to maintain sequence at head
        for (int i = batch.size() - 1; i >= 0; i--) {
            buffer.offerFirst(batch.get(i));
        }
        bufferSize.addAndGet(batch.size());
    }

    @Override
    public boolean isHealthy() {
        // Healthy if not full
        return bufferSize.get() < MAX_BUFFER_SIZE;
    }

    // Additional Helper for Shutdown
    public void clear() {
        buffer.clear();
        bufferSize.set(0);
    }
}
