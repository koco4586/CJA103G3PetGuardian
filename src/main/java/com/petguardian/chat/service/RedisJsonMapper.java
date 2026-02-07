package com.petguardian.chat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RedisJsonMapper {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisJsonMapper(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = createCleanMapper();
    }

    private ObjectMapper createCleanMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    // Lua script to atomically read -> modify -> write back JSON
    // ARGV[1]: preview
    // ARGV[2]: time (ISO string)
    // ARGV[3]: senderId
    // ARGV[4]: ttl (seconds)
    private static final String LUA_UPDATE_MESSAGE_META = """
            local key = KEYS[1]
            local preview = ARGV[1]
            local time = ARGV[2]
            local senderId = ARGV[3]
            local ttl = tonumber(ARGV[4])

            -- 1. Get existing JSON
            local json = redis.call('GET', key)
            if not json then
                return nil
            end

            -- 2. Decode to table
            local dto = cjson.decode(json)

            -- 3. Update fields (Matched with ChatRoomMetadataDTO fields)
            dto.lastMessagePreview = preview
            dto.lastMessageAt = time

            -- 4. Update Read Status
            -- Note: Lua arrays are 1-based
            local members = dto.memberIds
            if members and #members > 0 then
                if tostring(members[1]) == senderId then
                    dto.mem1LastReadAt = time
                elseif #members > 1 and tostring(members[2]) == senderId then
                    dto.mem2LastReadAt = time
                end
            end

            -- 5. Encode and Save
            local newJson = cjson.encode(dto)
            redis.call('SET', key, newJson, 'EX', ttl)

            return 'OK'
            """;

    private static final RedisScript<String> UPDATE_MSG_META_SCRIPT = new DefaultRedisScript<>(LUA_UPDATE_MESSAGE_META,
            String.class);

    // Atomic Read Status Update Script
    // ARGV[1]: userId
    // ARGV[2]: time (ISO string)
    // ARGV[3]: ttl (seconds)
    private static final String LUA_UPDATE_READ_STATUS = """
             local key = KEYS[1]
             local userId = ARGV[1]
             local time = ARGV[2]
             local ttl = tonumber(ARGV[3])
            \s
             -- 1. Get existing JSON
             local json = redis.call('GET', key)
             if not json then
                 return nil
             end
            \s
             -- 2. Decode
             local dto = cjson.decode(json)
            \s
             -- 3. Update ONLY Read Status matches
             local members = dto.memberIds
             local updated = false
            \s
             if members and #members > 0 then
                 if tostring(members[1]) == userId then
                     dto.mem1LastReadAt = time
                     updated = true
                 elseif #members > 1 and tostring(members[2]) == userId then
                     dto.mem2LastReadAt = time
                     updated = true
                 end
             end
            \s
             -- 4. Save only if needed
             if updated then
                 local newJson = cjson.encode(dto)
                 redis.call('SET', key, newJson, 'EX', ttl)
                 return 'OK'
             end
            \s
             return nil
             """;

    private static final RedisScript<String> UPDATE_READ_STATUS_SCRIPT = new DefaultRedisScript<>(
            LUA_UPDATE_READ_STATUS,
            String.class);

    // =================================================================================
    // CORE SERIALIZATION UTILS
    // =================================================================================

    public String toJson(Object value) {
        if (value == null)
            return null;
        try {
            if (value instanceof String)
                return (String) value; // Avoid double encoding if already string
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.error("[Redis] Serialization failed for {}: {}", value.getClass(), e.getMessage());
            throw new RuntimeException("Redis serialization failed", e);
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.debug("[Redis] Deserialization failed for {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    public <T> List<T> mget(List<String> keys, Class<T> clazz) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<String> jsonList = redisTemplate.opsForValue().multiGet(keys);
            if (jsonList == null || jsonList.isEmpty()) {
                return Collections.emptyList();
            }
            return jsonList.stream()
                    .map(json -> fromJson(json, clazz))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.debug("[Redis] mget failed for {}: {}", clazz.getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }

    public <T> T convertValue(Object fromValue, Class<T> toValueType) {
        if (fromValue == null)
            return null;
        try {
            return objectMapper.convertValue(fromValue, toValueType);
        } catch (Exception e) {
            log.debug("[Redis] Manual conversion to {} failed: {}", toValueType.getSimpleName(), e.getMessage());
            return null;
        }
    }

    public StringRedisTemplate getStringTemplate() {
        return redisTemplate;
    }

    // =================================================================================
    // HASH OPERATIONS (String-Based)
    // =================================================================================

    public <T> Optional<T> getHash(String key, Class<T> type) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty())
                return Optional.empty();

            // HASH STRATEGY: We store fields individualy.
            // Since we use StringRedisTemplate, it is Map<String, String>.
            // Jackson can convert Map<String, String> to POJO.
            return Optional.ofNullable(objectMapper.convertValue(entries, type));

        } catch (Exception e) {
            log.debug("[Redis] Failed to map hash to {}: {}", type.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    // Kept for compatibility if TypeReference is needed (though specific Class<T>
    // is preferred)
    public <T> Optional<T> getHash(String key, TypeReference<T> typeRef) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty())
                return Optional.empty();
            return Optional.ofNullable(objectMapper.convertValue(entries, typeRef));
        } catch (Exception e) {
            log.debug("[Redis] Failed to map hash (TypeRef): {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void setHash(String key, Object object, Duration ttl) {
        try {
            // Convert POJO to Map<String, String> to store as Hash fields
            Map<String, Object> rawMap = objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {
            });
            Map<String, String> stringMap = rawMap.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue() instanceof String ? (String) e.getValue()
                                    : String.valueOf(e.getValue())));

            redisTemplate.opsForHash().putAll(key, stringMap);
            if (ttl != null) {
                redisTemplate.expire(key, ttl);
            }
        } catch (Exception e) {
            log.debug("[Redis] Failed to store hash for key {}: {}", key, e.getMessage());
        }
    }

    // =================================================================================
    // LIST OPERATIONS
    // =================================================================================

    public <T> List<T> getList(String key, long start, long end, Class<T> type) {
        try {
            List<String> rawList = redisTemplate.opsForList().range(key, start, end);
            if (rawList == null || rawList.isEmpty())
                return Collections.emptyList();

            return rawList.stream()
                    .map(json -> fromJson(json, type))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.debug("[Redis] Failed to map list to {}: {}", type.getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }

    // =================================================================================
    // GENERAL OPERATIONS
    // =================================================================================

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public Long deleteBatch(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        // Use UNLINK (non-blocking delete) instead of DEL
        return redisTemplate.unlink(keys);
    }

    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }

    public String executeUpdateMessageMeta(String key, String preview, String time, Integer senderId, long ttlSeconds) {
        try {
            return redisTemplate.execute(
                    UPDATE_MSG_META_SCRIPT,
                    Collections.singletonList(key),
                    preview,
                    time,
                    String.valueOf(senderId),
                    String.valueOf(ttlSeconds));
        } catch (Exception e) {
            log.error("[Redis] Lua script execution failed: {}", e.getMessage());
            throw new RuntimeException("Redis Lua script failed", e);
        }
    }

    public boolean executeUpdateReadStatus(String key, Integer userId, String timeStr, long ttlSeconds) {
        try {
            String result = redisTemplate.execute(
                    UPDATE_READ_STATUS_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(userId),
                    timeStr,
                    String.valueOf(ttlSeconds));
            return "OK".equals(result);
        } catch (Exception e) {
            log.error("[Redis] Lua read-status script failed: {}", e.getMessage());
            throw new RuntimeException("Redis Lua script failed", e);
        }
    }
}
