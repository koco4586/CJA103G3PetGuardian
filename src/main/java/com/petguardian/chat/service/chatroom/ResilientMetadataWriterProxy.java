package com.petguardian.chat.service.chatroom;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceCircuitBreaker;

import lombok.extern.slf4j.Slf4j;

/**
 * Composite Metadata Writer Proxy.
 * Orchestrates Cache (Redis) and DB (MySQL) writing with Resilience.
 */
@Slf4j
@Service("metadataWriterProxy")
public class ResilientMetadataWriterProxy implements ChatRoomMetadataWriter {

    private final ChatRoomMetadataWriter primary; // Redis
    private final ChatRoomMetadataWriter secondary; // JPA
    private final ResilienceCircuitBreaker circuitBreaker;
    private final ResilienceChatFailureHandler failureHandler;

    public ResilientMetadataWriterProxy(
            @Qualifier("redisMetadataWriter") ChatRoomMetadataWriter primary,
            @Qualifier("jpaMetadataWriter") ChatRoomMetadataWriter secondary,
            ObjectProvider<ResilienceCircuitBreaker> circuitProvider,
            ResilienceChatFailureHandler failureHandler) {
        this.primary = primary;
        this.secondary = secondary;
        this.circuitBreaker = circuitProvider.getObject();
        this.failureHandler = failureHandler;
        this.circuitBreaker.setName("MetadataWriter-Circuit");
    }

    @Override
    public void syncRoomMetadata(Integer chatroomId, String preview, LocalDateTime time, Integer senderId) {
        secondary.syncRoomMetadata(chatroomId, preview, time, senderId);
        if (!circuitBreaker.isOpen()) {
            try {
                primary.syncRoomMetadata(chatroomId, preview, time, senderId);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
            }
        }
    }

    @Override
    public void updateCacheOnly(Integer chatroomId, String preview, LocalDateTime time, Integer senderId) {
        if (!circuitBreaker.isOpen()) {
            try {
                primary.updateCacheOnly(chatroomId, preview, time, senderId);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
            }
        }
    }

    @Override
    public void addUserToRoom(Integer userId, Integer chatroomId) {
        secondary.addUserToRoom(userId, chatroomId);
        if (!circuitBreaker.isOpen()) {
            try {
                primary.addUserToRoom(userId, chatroomId);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
            }
        }
    }

    @Override
    public void updateLastReadAt(Integer chatroomId, Integer userId, LocalDateTime time) {
        secondary.updateLastReadAt(chatroomId, userId, time);
        if (!circuitBreaker.isOpen()) {
            try {
                primary.updateLastReadAt(chatroomId, userId, time);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
            }
        }
    }

    @Override
    public void updateLastReadAtCacheOnly(Integer chatroomId, Integer userId, LocalDateTime time) {
        if (!circuitBreaker.isOpen()) {
            try {
                primary.updateLastReadAtCacheOnly(chatroomId, userId, time);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
            }
        }
    }

    @Override
    public void saveRoomMetadata(ChatRoomMetadataDTO meta) {
        if (!circuitBreaker.isOpen()) {
            try {
                primary.saveRoomMetadata(meta);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
            }
        }
        // secondary.saveRoomMetadata is usually no-op for JPA as it's the source
    }

    @Override
    public void saveMemberProfile(MemberProfileDTO profile) {
        if (!circuitBreaker.isOpen()) {
            try {
                primary.saveMemberProfile(profile);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
            }
        }
    }

    @Override
    public void saveUserRoomList(Integer userId, List<Integer> roomIds) {
        if (!circuitBreaker.isOpen()) {
            try {
                primary.saveUserRoomList(userId, roomIds);
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
            }
        }
    }

    @Override
    public boolean isHealthy() {
        return !circuitBreaker.isOpen();
    }

    @Override
    public boolean isConnectionException(Throwable e) {
        return failureHandler.isConnectionException(e);
    }

    @Override
    public boolean isDataIntegrityViolation(Throwable e) {
        return failureHandler.isDataIntegrityViolation(e);
    }

    private void recordFailure(Exception e) {
        if (failureHandler.isConnectionException(e)) {
            circuitBreaker.tripImmediately();
        } else {
            circuitBreaker.recordFailure(e);
        }
    }
}
