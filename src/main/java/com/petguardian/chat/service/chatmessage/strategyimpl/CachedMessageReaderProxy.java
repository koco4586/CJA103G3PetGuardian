package com.petguardian.chat.service.chatmessage.strategyimpl;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.service.chatmessage.MessageReaderStrategyService;
import com.petguardian.chat.service.chatmessage.ResilienceSupportStrategyService;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceCircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Composite Reader Proxy.
 * Orchestrates Cache (Redis) and DB (MySQL) reading.
 */
@Slf4j
@Service("messageReaderProxy")
public class CachedMessageReaderProxy implements MessageReaderStrategyService, ResilienceSupportStrategyService {

    private final MessageReaderStrategyService primary; // Redis
    private final MessageReaderStrategyService secondary; // MySQL
    private final ResilienceCircuitBreaker circuitBreaker;
    private final ResilienceChatFailureHandler failureHandler;

    public CachedMessageReaderProxy(
            @Qualifier("redisMessageReader") MessageReaderStrategyService primary,
            @Qualifier("mysqlMessageReader") MessageReaderStrategyService secondary,
            ObjectProvider<ResilienceCircuitBreaker> circuitProvider,
            ResilienceChatFailureHandler failureHandler) {
        this.primary = primary;
        this.secondary = secondary;
        this.circuitBreaker = circuitProvider.getObject();
        this.failureHandler = failureHandler;
        this.circuitBreaker.setName("ReaderProxy-Circuit");
    }

    @Override
    public List<ChatMessageEntity> findLatestMessages(Integer chatroomId, Pageable pageable) {
        if (circuitBreaker.isOpen()) {
            return secondary.findLatestMessages(chatroomId, pageable);
        }

        try {
            List<ChatMessageEntity> results = primary.findLatestMessages(chatroomId, pageable);
            circuitBreaker.recordSuccess();
            return results;
        } catch (Exception e) {
            log.error("[ReaderProxy] Primary Read Failed: {}", e.getMessage());
            if (failureHandler.isConnectionException(e)) {
                circuitBreaker.tripImmediately();
            } else {
                circuitBreaker.recordFailure(e);
            }
            return secondary.findLatestMessages(chatroomId, pageable);
        }
    }

    @Override
    public Optional<ChatMessageEntity> findById(String messageId) {
        if (circuitBreaker.isOpen()) {
            return secondary.findById(messageId);
        }
        try {
            return primary.findById(messageId);
        } catch (Exception e) {
            if (failureHandler.isConnectionException(e)) {
                circuitBreaker.tripImmediately();
            } else {
                circuitBreaker.recordFailure(e);
            }
            return secondary.findById(messageId);
        }
    }

    @Override
    public List<ChatMessageEntity> findAllById(Iterable<String> messageIds) {
        if (circuitBreaker.isOpen()) {
            return secondary.findAllById(messageIds);
        }
        try {
            return primary.findAllById(messageIds);
        } catch (Exception e) {
            if (failureHandler.isConnectionException(e)) {
                circuitBreaker.tripImmediately();
            } else {
                circuitBreaker.recordFailure(e);
            }
            return secondary.findAllById(messageIds);
        }
    }

    @Override
    public boolean isHealthy() {
        return !circuitBreaker.isOpen();
    }
}
