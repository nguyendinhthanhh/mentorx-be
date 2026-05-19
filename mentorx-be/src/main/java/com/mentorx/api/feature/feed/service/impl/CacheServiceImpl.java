package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.feature.feed.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of CacheService using Redis
 * Provides caching operations for personalized feed items
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@Service
@Profile("redis")
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final String FEED_KEY_PREFIX = "feed:user:";
    private static final String FEED_PATTERN = "feed:user:*";

    @Override
    public <T> void set(String key, T value) {
        set(key, value, DEFAULT_TTL);
    }

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cached value for key: {} with TTL: {}", key, ttl);
        } catch (Exception e) {
            log.error("Failed to cache value for key: {}", key, e);
            // Don't throw exception - cache failures should not break the application
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }
            log.debug("Cache hit for key: {}", key);
            return Optional.of((T) value);
        } catch (Exception e) {
            log.error("Failed to retrieve cached value for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted cache key: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete cache key: {}", key, e);
        }
    }

    @Override
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Deleted {} cache keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Failed to delete cache keys by pattern: {}", pattern, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check existence of cache key: {}", key, e);
            return false;
        }
    }

    @Override
    public String generateFeedKey(UUID userId) {
        return FEED_KEY_PREFIX + userId.toString();
    }

    @Override
    public String generateFeedKey(UUID userId, String itemType) {
        return FEED_KEY_PREFIX + userId.toString() + ":" + itemType.toLowerCase();
    }

    @Override
    public void invalidateUserFeed(UUID userId) {
        String pattern = FEED_KEY_PREFIX + userId.toString() + "*";
        deleteByPattern(pattern);
        log.info("Invalidated all feed caches for user: {}", userId);
    }

    @Override
    public void invalidateAllFeeds() {
        deleteByPattern(FEED_PATTERN);
        log.info("Invalidated all feed caches");
    }
}
