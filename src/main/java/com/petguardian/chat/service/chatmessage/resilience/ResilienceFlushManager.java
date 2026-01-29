package com.petguardian.chat.service.chatmessage.resilience;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.petguardian.chat.service.chatmessage.MessageCreationContext;
import com.petguardian.chat.service.chatmessage.MessageWriterStrategyService;

/**
 * Centralized Logic for Flushing Batches with Resilience.
 * 
 * Handles:
 * 1. Batch Persistence via Strategy.
 * 2. Circuit Breaker integration.
 * 3. Poison Pill Handling (Binary Split).
 * 4. Retry Queuing (Order Preservation).
 * 
 * Stateless Component.
 */
@Component
public class ResilienceFlushManager {

    private static final Logger log = LoggerFactory.getLogger(ResilienceFlushManager.class);

    /**
     * Processes a batch of messages with full error handling.
     * 
     * @param batch        The batch to persist.
     * @param target       The destination strategy (e.g. MySQL).
     * @param circuit      The circuit breaker to update/check.
     * @param retryHandler Logic to requeue failed messages (e.g. buffer.requeue()).
     * @return Number of successfully saved messages.
     */
    /**
     * Processes a batch of messages with full error handling.
     * 
     * @param batch        The batch to persist.
     * @param target       The destination strategy (Writer + Resilience).
     * @param circuit      The circuit breaker to update/check.
     * @param retryHandler Logic to requeue failed messages (e.g. buffer.requeue()).
     * @return Number of successfully saved messages.
     */
    public <T extends MessageWriterStrategyService> int processBatch(
            List<MessageCreationContext> batch,
            T target,
            ResilienceCircuitBreaker circuit,
            Consumer<List<MessageCreationContext>> retryHandler) {

        if (batch == null || batch.isEmpty())
            return 0;

        try {
            // Happy Path
            target.saveAll(batch);
            circuit.recordSuccess();
            return batch.size();
        } catch (Exception e) {
            // Failure Path
            if (target.isDataIntegrityViolation(e)) {
                log.error("Data Violation in Batch (Size: {}). Initiating Binary Split.", batch.size());
                return handlePoisonBatch(batch, target, retryHandler);
            } else {
                // Connection or Unknown: Retry Whole Batch
                log.warn("Batch Save Failed (Connection/Unknown/DataViolation). Requeuing {} messages. Error: {}",
                        batch.size(), e.getMessage());

                retryHandler.accept(batch);

                // Record failure in circuit breaker to trigger pause if persistence is broken
                circuit.recordFailure(e);

                return 0;
            }
        }
    }

    /**
     * Recursive Binary Split to isolate Poison Pills.
     */
    private <T extends MessageWriterStrategyService> int handlePoisonBatch(
            List<MessageCreationContext> batch,
            T target,
            Consumer<List<MessageCreationContext>> retryHandler) {

        if (batch.isEmpty())
            return 0;

        // Base Case: Single Item
        if (batch.size() == 1) {
            MessageCreationContext item = batch.get(0);
            try {
                target.save(item);
                return 1;
            } catch (Exception e) {
                if (target.isDataIntegrityViolation(e)) {
                    log.error("POISON PILL DISCARDED: ID={} Sender={}. Error: {}",
                            item.messageId(), item.senderId(), e.getMessage());
                    // Discard (Return 0, do not requeue)
                    return 0;
                } else {
                    // Connection error during hunt -> Retry this item
                    retryHandler.accept(batch);
                    return 0;
                }
            }
        }

        // Recursive Step
        int mid = batch.size() / 2;
        List<MessageCreationContext> firstHalf = batch.subList(0, mid);
        List<MessageCreationContext> secondHalf = batch.subList(mid, batch.size());

        int successCount = 0;

        // Try First Half
        successCount += trySaveOrRecurse(firstHalf, target, retryHandler);

        // Try Second Half
        successCount += trySaveOrRecurse(secondHalf, target, retryHandler);

        return successCount;
    }

    /**
     * Helper to avoid splitting purely transient connection errors.
     */
    private <T extends MessageWriterStrategyService> int trySaveOrRecurse(
            List<MessageCreationContext> subBatch,
            T target, Consumer<List<MessageCreationContext>> retryHandler) {
        try {
            target.saveAll(subBatch);
            return subBatch.size();
        } catch (Exception e) {
            if (target.isConnectionException(e)) {
                // Do not split connection errors, just requeue the chunk
                retryHandler.accept(subBatch);
                return 0;
            } else {
                // Data or Unknown -> Recurse/Split
                return handlePoisonBatch(subBatch, target, retryHandler);
            }
        }
    }
}
