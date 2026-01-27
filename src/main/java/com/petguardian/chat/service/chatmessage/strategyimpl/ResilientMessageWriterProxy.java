package com.petguardian.chat.service.chatmessage.strategyimpl;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.service.chatmessage.MessageBufferService;
import com.petguardian.chat.service.chatmessage.MessageCreationContext;
import com.petguardian.chat.service.chatmessage.MessageWriterStrategyService;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceCircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Composite Writer Proxy.
 * Orchestrates Primary (Redis) and Fallback (InMemory) writing.
 * Composes Resilience logic without inheritance.
 */
@Slf4j
@Service("messageWriterProxy")
public class ResilientMessageWriterProxy implements MessageWriterStrategyService {

    private final MessageWriterStrategyService primary;
    private final MessageBufferService fallbackBuffer;
    private final ResilienceCircuitBreaker circuitBreaker;
    private final ResilienceChatFailureHandler failureHandler;

    public ResilientMessageWriterProxy(
            @Qualifier("redisMessageWriter") MessageWriterStrategyService primary,
            @Qualifier("inMemoryBuffer") MessageBufferService fallbackBuffer,
            ObjectProvider<ResilienceCircuitBreaker> circuitProvider,
            ResilienceChatFailureHandler failureHandler) {
        this.primary = primary;
        this.fallbackBuffer = fallbackBuffer;
        this.circuitBreaker = circuitProvider.getObject();
        this.failureHandler = failureHandler;
        this.circuitBreaker.setName("WriterProxy-Circuit");
    }

    @Override
    public ChatMessageEntity save(MessageCreationContext context) {
        if (circuitBreaker.isOpen()) {
            fallbackBuffer.offer(0, context);
            return context.toEntity();
        }

        try {
            ChatMessageEntity result = primary.save(context);
            circuitBreaker.recordSuccess();
            return result;
        } catch (Exception e) {
            log.error("[WriterProxy] Primary Write Failed: {}", e.getMessage());
            if (failureHandler.isConnectionException(e)) {
                circuitBreaker.tripImmediately();
            } else {
                circuitBreaker.recordFailure(e);
            }
            fallbackBuffer.offer(0, context);
            return context.toEntity();
        }
    }

    @Override
    public void saveAll(List<MessageCreationContext> contexts) {
        if (circuitBreaker.isOpen()) {
            contexts.forEach(ctx -> fallbackBuffer.offer(0, ctx));
            return;
        }

        try {
            primary.saveAll(contexts);
            circuitBreaker.recordSuccess();
        } catch (Exception e) {
            log.error("[WriterProxy] Primary Bulk Write Failed: {}", e.getMessage());
            if (failureHandler.isConnectionException(e)) {
                circuitBreaker.tripImmediately();
            } else {
                circuitBreaker.recordFailure(e);
            }
            contexts.forEach(ctx -> fallbackBuffer.offer(0, ctx));
        }
    }

    @Override
    public boolean isAsyncPersistence() {
        return circuitBreaker.isOpen() || primary.isAsyncPersistence();
    }

    @Override
    public boolean isHealthy() {
        return !circuitBreaker.isOpen();
    }
}
