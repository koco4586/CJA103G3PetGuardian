package com.petguardian.chat.service.chatroom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceCircuitBreaker;

import lombok.extern.slf4j.Slf4j;

/**
 * Composite Metadata Reader Proxy.
 * Orchestrates Cache (Redis) and DB (MySQL) reading with Smart Tripping.
 */
@Slf4j
@Service("metadataReaderProxy")
public class CachedMetadataReaderProxy implements ChatRoomMetadataReader {

    private final ChatRoomMetadataReader primary; // Redis
    private final ChatRoomMetadataReader secondary; // JPA
    private final ChatRoomMetadataWriter primaryWriter; // Redis Writer (for read-through)
    private final ResilienceCircuitBreaker circuitBreaker;
    private final ResilienceChatFailureHandler failureHandler;

    public CachedMetadataReaderProxy(
            @Qualifier("redisMetadataReader") ChatRoomMetadataReader primary,
            @Qualifier("jpaMetadataReader") ChatRoomMetadataReader secondary,
            @Qualifier("redisMetadataWriter") ChatRoomMetadataWriter primaryWriter,
            ObjectProvider<ResilienceCircuitBreaker> circuitProvider,
            ResilienceChatFailureHandler failureHandler) {
        this.primary = primary;
        this.secondary = secondary;
        this.primaryWriter = primaryWriter;
        this.circuitBreaker = circuitProvider.getObject();
        this.failureHandler = failureHandler;
        this.circuitBreaker.setName("MetadataReader-Circuit");
    }

    @Override
    public ChatRoomMetadataDTO getRoomMetadata(Integer chatroomId) {
        if (!circuitBreaker.isOpen()) {
            try {
                ChatRoomMetadataDTO meta = primary.getRoomMetadata(chatroomId);
                if (meta != null) {
                    circuitBreaker.recordSuccess();
                    return meta;
                }
            } catch (Exception e) {
                recordFailure(e);
            }
        }

        ChatRoomMetadataDTO meta = secondary.getRoomMetadata(chatroomId);
        if (meta != null && !circuitBreaker.isOpen()) {
            try {
                primaryWriter.saveRoomMetadata(meta);
            } catch (Exception e) {
                recordFailure(e);
            }
        }
        return meta;
    }

    @Override
    public List<ChatRoomMetadataDTO> getUserChatrooms(Integer userId) {
        if (!circuitBreaker.isOpen()) {
            try {
                List<ChatRoomMetadataDTO> cached = primary.getUserChatrooms(userId);
                if (cached != null) {
                    circuitBreaker.recordSuccess();
                    return cached;
                }
            } catch (Exception e) {
                recordFailure(e);
            }
        }

        List<ChatRoomMetadataDTO> results = secondary.getUserChatrooms(userId);
        if (results != null && !results.isEmpty() && !circuitBreaker.isOpen()) {
            try {
                List<Integer> roomIds = results.stream().map(ChatRoomMetadataDTO::getChatroomId).toList();
                primaryWriter.saveUserRoomList(userId, roomIds);
                results.forEach(primaryWriter::saveRoomMetadata);
            } catch (Exception e) {
                recordFailure(e);
            }
        }
        return results;
    }

    @Override
    public MemberProfileDTO getMemberProfile(Integer memberId) {
        if (!circuitBreaker.isOpen()) {
            try {
                MemberProfileDTO profile = primary.getMemberProfile(memberId);
                if (profile != null) {
                    circuitBreaker.recordSuccess();
                    return profile;
                }
            } catch (Exception e) {
                recordFailure(e);
            }
        }

        MemberProfileDTO profile = secondary.getMemberProfile(memberId);
        if (profile != null && !circuitBreaker.isOpen()) {
            try {
                primaryWriter.saveMemberProfile(profile);
            } catch (Exception e) {
                recordFailure(e);
            }
        }
        return profile;
    }

    @Override
    public Map<Integer, MemberProfileDTO> getMemberProfiles(List<Integer> memberIds) {
        Map<Integer, MemberProfileDTO> results = new HashMap<>();
        List<Integer> missingIds = new ArrayList<>();

        if (!circuitBreaker.isOpen()) {
            try {
                Map<Integer, MemberProfileDTO> cached = primary.getMemberProfiles(memberIds);
                results.putAll(cached);
                for (Integer id : memberIds) {
                    if (!results.containsKey(id))
                        missingIds.add(id);
                }
                circuitBreaker.recordSuccess();
            } catch (Exception e) {
                recordFailure(e);
                missingIds.addAll(memberIds);
            }
        } else {
            missingIds.addAll(memberIds);
        }

        if (!missingIds.isEmpty()) {
            Map<Integer, MemberProfileDTO> dbData = secondary.getMemberProfiles(missingIds);
            results.putAll(dbData);
            if (!circuitBreaker.isOpen()) {
                try {
                    dbData.values().forEach(primaryWriter::saveMemberProfile);
                } catch (Exception e) {
                    recordFailure(e);
                }
            }
        }
        return results;
    }

    @Override
    public Optional<ChatRoomMetadataDTO> findRoomByMembers(Integer memId1, Integer memId2) {
        if (!circuitBreaker.isOpen()) {
            try {
                Optional<ChatRoomMetadataDTO> result = primary.findRoomByMembers(memId1, memId2);
                if (result.isPresent())
                    return result;
            } catch (Exception e) {
                recordFailure(e);
            }
        }
        return secondary.findRoomByMembers(memId1, memId2);
    }

    private void recordFailure(Exception e) {
        if (failureHandler.isConnectionException(e)) {
            circuitBreaker.tripImmediately();
        } else {
            circuitBreaker.recordFailure(e);
        }
    }
}
