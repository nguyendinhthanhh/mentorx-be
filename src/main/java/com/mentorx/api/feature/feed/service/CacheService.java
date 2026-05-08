package com.mentorx.api.feature.feed.service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for caching operations
 * Provides methods for storing and retrieving cached data with TTL support
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
public interface CacheService {

    /**
     * Store a value in cache with default TTL (1 hour)
     * 
     * @param key cache key
     * @param value value to cache
     * @param <T> type of value
     */
    <T> void set(String key, T value);

    /**
     * Store a value in cache with custom TTL
     * 
     * @param key cache key
     * @param value value to cache
     * @param ttl time-to-live duration
     * @param <T> type of value
     */
    <T> void set(String key, T value, Duration ttl);

    /**
     * Retrieve a value from cache
     * 
     * @param key cache key
     * @param clazz class type of value
     * @param <T> type of value
     * @return Optional containing the cached value, or empty if not found
     */
    <T> Optional<T> get(String key, Class<T> clazz);

    /**
     * Delete a value from cache
     * 
     * @param key cache key
     */
    void delete(String key);

    /**
     * Delete all cache entries matching a pattern
     * 
     * @param pattern key pattern (e.g., "feed:user:*")
     */
    void deleteByPattern(String pattern);

    /**
     * Check if a key exists in cache
     * 
     * @param key cache key
     * @return true if key exists, false otherwise
     */
    boolean exists(String key);

    /**
     * Generate cache key for user feed
     * 
     * @param userId user ID
     * @return cache key in format "feed:user:{userId}"
     */
    String generateFeedKey(UUID userId);

    /**
     * Generate cache key for user feed by type
     * 
     * @param userId user ID
     * @param itemType feed item type
     * @return cache key in format "feed:user:{userId}:{itemType}"
     */
    String generateFeedKey(UUID userId, String itemType);

    /**
     * Invalidate all feed caches for a user
     * 
     * @param userId user ID
     */
    void invalidateUserFeed(UUID userId);

    /**
     * Invalidate all feed caches (used after background job)
     */
    void invalidateAllFeeds();
}
