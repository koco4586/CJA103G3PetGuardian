package com.petguardian.chat.service.chatroom;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis implementation for Metadata Reading.
 */
@Slf4j
@Service("redisMetadataReader")
@RequiredArgsConstructor
public class RedisMetadataReader implements ChatRoomMetadataReader {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ROOM_META_KEY = "chat:room:%d:meta";
    private static final String MEMBER_META_KEY = "chat:member:%d:meta";
    private static final String USER_ROOMS_KEY = "chat:user:%d:rooms";

    @Override
    public ChatRoomMetadataDTO getRoomMetadata(Integer chatroomId) {
        String key = String.format(ROOM_META_KEY, chatroomId);
        Object data = redisTemplate.opsForValue().get(key);
        if (data instanceof ChatRoomMetadataDTO meta) {
            return meta;
        }
        return null;
    }

    @Override
    public List<ChatRoomMetadataDTO> getUserChatrooms(Integer userId) {
        String listKey = String.format(USER_ROOMS_KEY, userId);

        if (Boolean.FALSE.equals(redisTemplate.hasKey(listKey))) {
            return null; // Cache Miss
        }

        java.util.Set<Object> roomIds = redisTemplate.opsForSet().members(listKey);
        if (roomIds == null || roomIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> metaKeys = roomIds.stream()
                .map(id -> String.format(ROOM_META_KEY, (Integer) id))
                .toList();

        List<Object> values = redisTemplate.opsForValue().multiGet(metaKeys);
        if (values == null)
            return Collections.emptyList();

        return values.stream()
                .filter(v -> v instanceof ChatRoomMetadataDTO)
                .map(v -> (ChatRoomMetadataDTO) v)
                .collect(Collectors.toList());
    }

    @Override
    public MemberProfileDTO getMemberProfile(Integer memberId) {
        String key = String.format(MEMBER_META_KEY, memberId);
        Object data = redisTemplate.opsForValue().get(key);
        if (data instanceof MemberProfileDTO profile) {
            return profile;
        }
        return null;
    }

    @Override
    public Map<Integer, MemberProfileDTO> getMemberProfiles(List<Integer> memberIds) {
        if (memberIds == null || memberIds.isEmpty())
            return Collections.emptyMap();

        List<String> keys = memberIds.stream()
                .map(id -> String.format(MEMBER_META_KEY, id))
                .toList();

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null)
            return Collections.emptyMap();

        return values.stream()
                .filter(v -> v instanceof MemberProfileDTO)
                .map(v -> (MemberProfileDTO) v)
                .collect(Collectors.toMap(MemberProfileDTO::getMemberId, p -> p));
    }

    @Override
    public Optional<ChatRoomMetadataDTO> findRoomByMembers(Integer memId1, Integer memId2) {
        String listKey1 = String.format(USER_ROOMS_KEY, memId1);
        String listKey2 = String.format(USER_ROOMS_KEY, memId2);

        java.util.Set<Object> commonRoomIds = redisTemplate.opsForSet().intersect(listKey1, listKey2);
        if (commonRoomIds == null || commonRoomIds.isEmpty()) {
            return Optional.empty();
        }

        Object firstId = commonRoomIds.iterator().next();
        Integer chatroomId = (firstId instanceof Integer) ? (Integer) firstId : Integer.parseInt(firstId.toString());
        return Optional.ofNullable(getRoomMetadata(chatroomId));
    }
}
