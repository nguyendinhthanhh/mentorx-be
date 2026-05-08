package com.mentorx.api.feature.feed.service.impl;

import com.mentorx.api.feature.feed.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Local cache fallback used when Redis is not enabled.
 */
@Slf4j
@Service
@Profile("!redis")
public class InMemoryCacheServiceImpl implements CacheService {

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final String FEED_KEY_PREFIX = "feed:user:";
    private static final String FEED_PATTERN = "feed:user:*";

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public <T> void set(String key, T value) {
        set(key, value, DEFAULT_TTL);
    }

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        cache.put(key, new CacheEntry(value, Instant.now().plus(ttl)));
        log.debug("Cached value in memory for key: {} with TTL: {}", key, ttl);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }

        if (entry.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }

        Object value = entry.value();
        if (!clazz.isInstance(value)) {
            log.warn("Cached value for key {} has type {}, expected {}", key, value.getClass().getName(), clazz.getName());
            return Optional.empty();
        }

        return Optional.of(clazz.cast(value));
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }

    @Override
    public void deleteByPattern(String pattern) {
        Pattern regex = Pattern.compile(pattern.replace("*", ".*"));
        cache.keySet().removeIf(key -> regex.matcher(key).matches());
    }

    @Override
    public boolean exists(String key) {
        return get(key, Object.class).isPresent();
    }

    @Override
    public String generateFeedKey(UUID userId) {
        return FEED_KEY_PREFIX + userId;
    }

    @Override
    public String generateFeedKey(UUID userId, String itemType) {
        return FEED_KEY_PREFIX + userId + ":" + itemType.toLowerCase();
    }

    @Override
    public void invalidateUserFeed(UUID userId) {
        deleteByPattern(FEED_KEY_PREFIX + userId + "*");
    }

    @Override
    public void invalidateAllFeeds() {
        deleteByPattern(FEED_PATTERN);
    }

    private record CacheEntry(Object value, Instant expiresAt) {
        private boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
