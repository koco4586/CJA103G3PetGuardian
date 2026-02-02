package com.petguardian.chat.service.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core Infrastructure: Type-Safe Redis JSON Mapper.
 * 
 * Provides explicit mapping between Redis (Clean JSON) and Java Types.
 * Eliminates @class polymorphic metadata in favor of explicit
 * TypeReference/Class passing.
 */
@Slf4j
@Component
public class RedisJsonMapper {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisJsonMapper(RedisTemplate<String, Object> redisTemplate) {
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
    // HASH OPERATIONS
    // =================================================================================

    public <T> Optional<T> getHash(String key, Class<T> type) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries == null || entries.isEmpty())
                return Optional.empty();

            return Optional.ofNullable(objectMapper.convertValue(entries, type));
        } catch (Exception e) {
            log.debug("[Redis] Failed to map hash to {}: {}", type.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    public <T> Optional<T> getHash(String key, TypeReference<T> typeRef) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries == null || entries.isEmpty())
                return Optional.empty();

            return Optional.ofNullable(objectMapper.convertValue(entries, typeRef));
        } catch (Exception e) {
            log.debug("[Redis] Failed to map hash to complex type: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void setHash(String key, Object object, Duration ttl) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.convertValue(object, Map.class);
            redisTemplate.opsForHash().putAll(key, map);
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
            List<Object> rawList = redisTemplate.opsForList().range(key, start, end);
            if (rawList == null || rawList.isEmpty())
                return Collections.emptyList();

            return rawList.stream()
                    .map(item -> objectMapper.convertValue(item, type))
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

    /**
     * Explicitly converts a raw object (e.g. Map from Redis) to a specific type.
     * Useful for background workers handling non-typed objects.
     */
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

    /**
     * Internal access to template for complex operations (Lua, Pipelines).
     */
    public RedisTemplate<String, Object> getTemplate() {
        return redisTemplate;
    }
}
