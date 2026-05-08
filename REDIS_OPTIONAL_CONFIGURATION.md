# Redis Optional Configuration - Complete Guide

## Date: May 7, 2026

## Problem Solved
Application was failing to start with:
```
org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis
Connection refused: localhost:6379
```

## Solution Implemented
Made Redis **optional** using Spring Profiles. The application now:
- ✅ Starts successfully **without Redis** (uses simple in-memory cache)
- ✅ Can enable Redis when needed by activating the "redis" profile
- ✅ Maintains all caching functionality in both modes

---

## How It Works

### Default Behavior (No Redis Required)
```yaml
# application.yml
spring:
  cache:
    type: simple  # In-memory cache (no Redis needed)
```

**Result**: Application starts immediately, caching works in-memory.

### With Redis Enabled
```yaml
# application-redis.yml (activated with "redis" profile)
spring:
  cache:
    type: redis  # Use Redis for distributed caching
```

**Result**: Application uses Redis for caching (requires Redis to be running).

---

## Files Modified

### 1. RedisConfig.java
**Change**: Added `@Profile("redis")` annotation

```java
@Configuration
@EnableCaching
@Profile("redis")  // ← Only active when "redis" profile is enabled
public class RedisConfig {
    // ... configuration
}
```

**Impact**: Redis beans are only created when the "redis" profile is active.

### 2. application.yml
**Change**: Changed cache type from `redis` to `simple`

```yaml
spring:
  cache:
    type: simple  # ← Changed from "redis" to "simple"
```

**Impact**: Uses in-memory cache by default (no external dependencies).

### 3. application-redis.yml (NEW)
**Purpose**: Profile-specific configuration for Redis

```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379
```

**Impact**: Overrides cache type to Redis when profile is active.

---

## Usage Guide

### Option 1: Run Without Redis (Default)

**Command**:
```bash
./mvnw spring-boot:run
```

**Result**:
- ✅ Application starts immediately
- ✅ Uses simple in-memory cache
- ✅ All features work (caching is local)
- ⚠️ Cache is not shared across instances
- ⚠️ Cache is lost on restart

**Best For**:
- Local development
- Testing
- Single-instance deployments
- When Redis is not available

---

### Option 2: Run With Redis

#### Step 1: Start Redis

**Using Docker** (Recommended):
```bash
# Start Docker Desktop first, then:
docker-compose up -d redis

# Verify Redis is running:
docker ps | grep redis
```

**Using Local Redis**:
```bash
# Windows: Run redis-server.exe
# Linux: sudo systemctl start redis
# Mac: brew services start redis
```

#### Step 2: Run Application with Redis Profile

**Command**:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,redis
```

**Or set in application.yml**:
```yaml
spring:
  profiles:
    active: dev,redis
```

**Result**:
- ✅ Application connects to Redis
- ✅ Uses Redis for distributed caching
- ✅ Cache persists across restarts
- ✅ Cache can be shared across multiple instances
- ✅ Better performance for repeated requests

**Best For**:
- Production deployments
- Multi-instance deployments
- When you need persistent cache
- When you need distributed cache

---

## Verification

### Check Active Profiles
```bash
# Application logs will show:
The following profiles are active: dev,redis
```

### Check Cache Type
```bash
# Without Redis profile:
Using cache type: simple

# With Redis profile:
Using cache type: redis
```

### Test Redis Connection
```bash
# If Redis profile is active:
docker exec -it mentorx-redis redis-cli ping
# Should return: PONG
```

---

## Cache Configuration

### Cached Items
Both simple and Redis cache support these cache names:
- `users` - User data
- `mentors` - Mentor profiles
- `jobs` - Job postings
- `courses` - Course information
- `match-scores` - Matching algorithm scores
- `feed-items` - Personalized feed items

### Cache TTL (Redis Only)
```yaml
cache:
  feed:
    ttl-hours: 1  # Feed items expire after 1 hour
```

**Note**: Simple cache does not support TTL (items stay until restart or manual eviction).

---

## Docker Compose Configuration

### Start Only Redis
```bash
docker-compose up -d redis
```

### Start All Services (Including Redis)
```bash
docker-compose up -d
```

### Stop Redis
```bash
docker-compose stop redis
```

### View Redis Logs
```bash
docker logs mentorx-redis
```

### Connect to Redis CLI
```bash
docker exec -it mentorx-redis redis-cli
```

### Clear Redis Cache
```bash
docker exec -it mentorx-redis redis-cli FLUSHALL
```

---

## Performance Comparison

### Simple Cache (In-Memory)
- ✅ **Pros**: No setup, fast, no external dependencies
- ⚠️ **Cons**: Not shared, lost on restart, limited memory

### Redis Cache
- ✅ **Pros**: Distributed, persistent, scalable, shared across instances
- ⚠️ **Cons**: Requires Redis server, network latency, additional complexity

---

## Troubleshooting

### Issue: Application won't start (Redis connection error)
**Solution**: Redis profile is active but Redis is not running
```bash
# Option 1: Start Redis
docker-compose up -d redis

# Option 2: Disable Redis profile
# Remove "redis" from spring.profiles.active in application.yml
```

### Issue: Cache not working
**Check**:
1. Verify `@EnableCaching` is present
2. Check if methods have `@Cacheable` annotation
3. Verify cache names match configuration

### Issue: Redis cache not persisting
**Check**:
1. Verify Redis profile is active
2. Check Redis connection: `docker exec -it mentorx-redis redis-cli ping`
3. Check TTL configuration

---

## Environment Variables

### Redis Configuration
```bash
REDIS_HOST=localhost        # Redis server host
REDIS_PORT=6379            # Redis server port
REDIS_PASSWORD=            # Redis password (optional)
REDIS_DATABASE=0           # Redis database number
```

### Profile Activation
```bash
SPRING_PROFILES_ACTIVE=dev,redis  # Activate Redis profile
```

---

## Production Recommendations

### Development
- Use **simple cache** (no Redis needed)
- Fast startup, easy debugging

### Staging
- Use **Redis** with docker-compose
- Test distributed caching behavior

### Production
- Use **Redis** with managed service (AWS ElastiCache, Azure Cache for Redis, etc.)
- Configure connection pooling
- Set appropriate TTL values
- Monitor cache hit rates
- Enable Redis persistence (RDB/AOF)

---

## Summary

### ✅ What Was Fixed
1. Made Redis optional via Spring Profile
2. Application now starts without Redis
3. Simple in-memory cache used by default
4. Redis can be enabled when needed

### ✅ Benefits
- No Redis dependency for local development
- Faster startup time
- Easier onboarding for new developers
- Production-ready Redis configuration available when needed

### ✅ How to Use
- **Default**: Just run `./mvnw spring-boot:run` (no Redis needed)
- **With Redis**: Run `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,redis`

---

## Quick Reference

```bash
# Run without Redis (default)
./mvnw spring-boot:run

# Run with Redis
docker-compose up -d redis
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,redis

# Check Redis status
docker ps | grep redis

# Test Redis connection
docker exec -it mentorx-redis redis-cli ping

# View cached keys
docker exec -it mentorx-redis redis-cli KEYS "*"

# Clear cache
docker exec -it mentorx-redis redis-cli FLUSHALL
```

---

**Status**: ✅ **COMPLETE** - Application now works with or without Redis
