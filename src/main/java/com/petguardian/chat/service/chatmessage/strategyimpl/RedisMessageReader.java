package com.petguardian.chat.service.chatmessage.strategyimpl;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.service.chatmessage.MessageReaderStrategyService;
import com.petguardian.chat.service.chatmessage.ResilienceSupportStrategyService;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Redis Implementation of Message Reading.
 * Handles Cache-Aside (Cold Start Protection).
 */
@Slf4j
@Service("redisMessageReader")
public class RedisMessageReader implements MessageReaderStrategyService, ResilienceSupportStrategyService {

    private static final String ROOM_MSGS_Key = "chat:room:%d:msgs";
    private static final String LOCK_KEY = "lock:chat:room:%d";
    private static final String RECENT_MSGS_HASH = "chat:recent_msgs";
    private static final long CACHE_TTL_SECONDS = 86400;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomMetadataReader metadataReader;
    private final MessageReaderStrategyService secondaryReader;

    public RedisMessageReader(
            @Lazy RedisTemplate<String, Object> redisTemplate,
            @Qualifier("metadataReaderProxy") ChatRoomMetadataReader metadataReader,
            @Qualifier("mysqlMessageReader") MessageReaderStrategyService secondaryReader) {
        this.redisTemplate = redisTemplate;
        this.metadataReader = metadataReader;
        this.secondaryReader = secondaryReader;
    }

    @Override
    public List<ChatMessageEntity> findLatestMessages(Integer chatroomId, Pageable pageable) {
        if (pageable.getPageNumber() > 0) {
            return secondaryReader.findLatestMessages(chatroomId, pageable);
        }

        String cacheKey = String.format(ROOM_MSGS_Key, chatroomId);
        List<Object> cached = redisTemplate.opsForList().range(cacheKey, 0, -1);

        if (cached != null && !cached.isEmpty()) {
            return castList(cached);
        }

        log.info("[Redis-Reader] Cache Miss for Room: {}. Entering Cold Start...", chatroomId);
        return handleColdStart(chatroomId, pageable, cacheKey);
    }

    private List<ChatMessageEntity> handleColdStart(Integer chatroomId, Pageable pageable, String cacheKey) {
        String lockKey = String.format(LOCK_KEY, chatroomId);
        boolean acquired = Boolean.TRUE
                .equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(5)));

        if (acquired) {
            try {
                List<Object> cached = redisTemplate.opsForList().range(cacheKey, 0, -1);
                if (cached != null && !cached.isEmpty())
                    return castList(cached);

                List<ChatMessageEntity> dbData = secondaryReader.findLatestMessages(chatroomId, pageable);
                if (dbData.isEmpty())
                    return dbData;

                redisTemplate.opsForList().rightPushAll(cacheKey, (Object[]) dbData.toArray(new ChatMessageEntity[0]));
                redisTemplate.expire(cacheKey, Duration.ofSeconds(CACHE_TTL_SECONDS));

                ChatRoomMetadataDTO meta = metadataReader.getRoomMetadata(chatroomId);
                if (meta != null && meta.getMemberIds() != null) {
                    metadataReader.getMemberProfiles(meta.getMemberIds());
                }
                return dbData;
            } finally {
                redisTemplate.delete(lockKey);
            }
        }
        return secondaryReader.findLatestMessages(chatroomId, pageable);
    }

    @Override
    public Optional<ChatMessageEntity> findById(String messageId) {
        try {
            Object cached = redisTemplate.opsForHash().get(RECENT_MSGS_HASH, messageId);
            if (cached instanceof ChatMessageEntity entity)
                return Optional.of(entity);
        } catch (Exception e) {
            log.warn("[Redis-Reader] Cache lookup failed for {}: {}", messageId, e.getMessage());
        }
        return secondaryReader.findById(messageId);
    }

    @Override
    public List<ChatMessageEntity> findAllById(Iterable<String> messageIds) {
        return secondaryReader.findAllById(messageIds);
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessageEntity> castList(List<Object> objects) {
        return (List<ChatMessageEntity>) (List<?>) objects;
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
}
