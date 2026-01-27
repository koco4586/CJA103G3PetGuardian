package com.petguardian.chat.service.chatmessage.strategyimpl;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.service.chatmessage.MessageBufferService;
import com.petguardian.chat.service.chatmessage.MessageCreationContext;
import com.petguardian.chat.service.chatmessage.MessageWriterStrategyService;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Redis Implementation of Message Writing.
 * Handles Atomic Cache Push + Write-Behind Buffer.
 */
@Slf4j
@Service("redisMessageWriter")
public class RedisMessageWriter implements MessageWriterStrategyService {

    private static final String ROOM_MSGS_Key = "chat:room:%d:msgs";
    private static final String RECENT_MSGS_HASH = "chat:recent_msgs";
    private static final int SHARD_COUNT = 10;
    private static final int CACHE_LIMIT = 50;
    private static final long CACHE_TTL_SECONDS = 86400;

    private static final String PUSH_TRIM_EXPIRE_LUA = "redis.call('LPUSH', KEYS[1], ARGV[1])\n" +
            "redis.call('LTRIM', KEYS[1], 0, ARGV[2] - 1)\n" +
            "redis.call('EXPIRE', KEYS[1], ARGV[3])\n" +
            "return redis.call('LLEN', KEYS[1])";

    private static final RedisScript<Long> PUSH_TRIM_EXPIRE_SCRIPT = new DefaultRedisScript<>(PUSH_TRIM_EXPIRE_LUA,
            Long.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageBufferService bufferService;
    private final ChatRoomMetadataWriter metadataWriter;
    private final ResilienceChatFailureHandler failureHandler;

    public RedisMessageWriter(
            @Lazy RedisTemplate<String, Object> redisTemplate,
            @Qualifier("redisMessageBufferServiceImpl") MessageBufferService bufferService,
            @Qualifier("metadataWriterProxy") ChatRoomMetadataWriter metadataWriter,
            ResilienceChatFailureHandler failureHandler) {
        this.redisTemplate = redisTemplate;
        this.bufferService = bufferService;
        this.metadataWriter = metadataWriter;
        this.failureHandler = failureHandler;
    }

    @Override
    public ChatMessageEntity save(MessageCreationContext context) {
        int shardId = context.chatroomId() % SHARD_COUNT;
        bufferService.offer(shardId, context);

        String cacheKey = String.format(ROOM_MSGS_Key, context.chatroomId());
        ChatMessageEntity entity = context.toEntity();

        try {
            Long currentSize = redisTemplate.execute(
                    PUSH_TRIM_EXPIRE_SCRIPT,
                    Collections.singletonList(cacheKey),
                    entity,
                    CACHE_LIMIT,
                    CACHE_TTL_SECONDS);
            log.info("[Redis-Writer][{}] Save - Lua Result Size: {}", Thread.currentThread().getName(), currentSize);

            redisTemplate.opsForHash().put(RECENT_MSGS_HASH, entity.getMessageId(), entity);
            redisTemplate.expire(RECENT_MSGS_HASH, Duration.ofSeconds(CACHE_TTL_SECONDS));
        } catch (Exception e) {
            log.error("[Redis-Writer] Save - Lua Script Failed: {}", e.getMessage());
        }

        metadataWriter.updateCacheOnly(context.chatroomId(), entity.getMessage(), entity.getChatTime(),
                entity.getMemberId());
        return entity;
    }

    @Override
    public void saveAll(List<MessageCreationContext> contexts) {
        contexts.forEach(this::save);
    }

    @Override
    public boolean isAsyncPersistence() {
        return true;
    }

    @Override
    public boolean isHealthy() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isConnectionException(Throwable e) {
        return failureHandler.isConnectionException(e);
    }

    @Override
    public boolean isDataIntegrityViolation(Throwable e) {
        return failureHandler.isDataIntegrityViolation(e);
    }
}
