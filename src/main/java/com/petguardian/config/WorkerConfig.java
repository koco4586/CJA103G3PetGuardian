package com.petguardian.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.petguardian.chat.service.chatmessage.MessageBufferService;
import com.petguardian.chat.service.chatmessage.MessageWriterStrategyService;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceCircuitBreaker;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceFlowControl;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceFlushManager;
import com.petguardian.chat.worker.AsyncBatchPersistenceWorker;

@Configuration
public class WorkerConfig {

    /**
     * Primary Worker: Drains Redis Buffer -> Persists to MySQL.
     * Handles the high-volume write-behind traffic.
     */
    @Bean
    public AsyncBatchPersistenceWorker redisToMysqlWorker(
            @Qualifier("redisMessageBufferServiceImpl") MessageBufferService redisBuffer,
            @Qualifier("mysqlMessageWriter") MessageWriterStrategyService mysqlWriter,
            ResilienceFlushManager flushManager,
            ResilienceFlowControl flowControl,
            ObjectProvider<ResilienceCircuitBreaker> circuitProvider,
            ResilienceChatFailureHandler failureHandler) {

        return new AsyncBatchPersistenceWorker(
                "RedisWorker",
                redisBuffer,
                mysqlWriter,
                flushManager,
                flowControl,
                circuitProvider,
                failureHandler);
    }

    /**
     * Secondary Worker: Drains InMemory Buffer -> Persists to MySQL.
     * Handles the Fallback traffic when Redis is down.
     * Replaces the old Scheduled Task in Proxy.
     */
    @Bean
    public AsyncBatchPersistenceWorker memoryToMysqlWorker(
            @Qualifier("inMemoryBuffer") MessageBufferService memoryBuffer,
            @Qualifier("mysqlMessageWriter") MessageWriterStrategyService mysqlWriter,
            ResilienceFlushManager flushManager,
            ResilienceFlowControl flowControl,
            ObjectProvider<ResilienceCircuitBreaker> circuitProvider,
            ResilienceChatFailureHandler failureHandler) {

        return new AsyncBatchPersistenceWorker(
                "MemoryWorker",
                memoryBuffer,
                mysqlWriter,
                flushManager,
                flowControl,
                circuitProvider,
                failureHandler);
    }
}
