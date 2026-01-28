package com.petguardian.chat.service.chatroom;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;

import org.springframework.beans.factory.annotation.Qualifier;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis implementation for Metadata Writing.
 */
@Slf4j
@Service("redisMetadataWriter")
public class RedisMetadataWriter implements ChatRoomMetadataWriter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ResilienceChatFailureHandler failureHandler;
    private final ChatRoomMetadataReader reader; // For internal cache lookup

    public RedisMetadataWriter(
            RedisTemplate<String, Object> redisTemplate,
            ResilienceChatFailureHandler failureHandler,
            @Qualifier("redisMetadataReader") ChatRoomMetadataReader reader) {
        this.redisTemplate = redisTemplate;
        this.failureHandler = failureHandler;
        this.reader = reader;
    }

    private static final String ROOM_META_KEY = "chat:room:%d:meta";
    private static final String MEMBER_META_KEY = "chat:member:%d:meta";
    private static final String USER_ROOMS_KEY = "chat:user:%d:rooms";
    private static final long TTL_SECONDS = 86400;

    @Override
    public void syncRoomMetadata(Integer chatroomId, String preview, LocalDateTime time, Integer senderId) {
        ChatRoomMetadataDTO meta = reader.getRoomMetadata(chatroomId);
        if (meta != null) {
            meta.setLastMessagePreview(preview);
            meta.setLastMessageAt(time);
            if (senderId != null) {
                if (senderId.equals(meta.getMemberIds().get(0))) {
                    meta.setMem1LastReadAt(time);
                } else if (senderId.equals(meta.getMemberIds().get(1))) {
                    meta.setMem2LastReadAt(time);
                }
            }
            saveRoomMetadata(meta);
        }
    }

    @Override
    public void updateCacheOnly(Integer chatroomId, String preview, LocalDateTime time, Integer senderId) {
        syncRoomMetadata(chatroomId, preview, time, senderId);
    }

    @Override
    public void addUserToRoom(Integer userId, Integer chatroomId) {
        String key = String.format(USER_ROOMS_KEY, userId);
        redisTemplate.opsForSet().add(key, chatroomId);
        redisTemplate.expire(key, Duration.ofSeconds(TTL_SECONDS));
    }

    @Override
    public void updateLastReadAt(Integer chatroomId, Integer userId, LocalDateTime time) {
        ChatRoomMetadataDTO meta = reader.getRoomMetadata(chatroomId);
        if (meta != null) {
            if (userId.equals(meta.getMemberIds().get(0))) {
                meta.setMem1LastReadAt(time);
            } else if (userId.equals(meta.getMemberIds().get(1))) {
                meta.setMem2LastReadAt(time);
            }
            saveRoomMetadata(meta);
        }
    }

    @Override
    public void updateLastReadAtCacheOnly(Integer chatroomId, Integer userId, LocalDateTime time) {
        updateLastReadAt(chatroomId, userId, time);
    }

    @Override
    public void saveRoomMetadata(ChatRoomMetadataDTO meta) {
        if (meta == null)
            return;
        String key = String.format(ROOM_META_KEY, meta.getChatroomId());
        redisTemplate.opsForValue().set(key, meta, Duration.ofSeconds(TTL_SECONDS));
    }

    @Override
    public void saveMemberProfile(MemberProfileDTO profile) {
        if (profile == null)
            return;
        String key = String.format(MEMBER_META_KEY, profile.getMemberId());
        redisTemplate.opsForValue().set(key, profile, Duration.ofSeconds(TTL_SECONDS));
    }

    @Override
    public void saveUserRoomList(Integer userId, List<Integer> roomIds) {
        if (userId == null || roomIds == null)
            return;
        String key = String.format(USER_ROOMS_KEY, userId);
        redisTemplate.delete(key);
        if (!roomIds.isEmpty()) {
            redisTemplate.opsForSet().add(key, roomIds.toArray(new Object[0]));
            redisTemplate.expire(key, Duration.ofSeconds(TTL_SECONDS));
        }
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
