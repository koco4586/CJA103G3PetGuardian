package com.petguardian.chat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

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

    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }
}
