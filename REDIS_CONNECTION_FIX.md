# Redis Connection Issue - Solutions

## Error
```
org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis
Caused by: java.net.ConnectException: Connection refused: localhost:6379
```

## Root Cause
The application is configured to use Redis for caching, but Redis server is not running.

---

## Solution 1: Start Redis with Docker (Recommended)

### Prerequisites
- Docker Desktop must be running

### Steps

1. **Start Docker Desktop**
   - Open Docker Desktop application
   - Wait for it to fully start (whale icon in system tray should be stable)

2. **Start Redis Container**
   ```bash
   cd mentorx-be
   docker-compose up -d redis
   ```

3. **Verify Redis is Running**
   ```bash
   docker ps | grep redis
   ```
   
   You should see:
   ```
   mentorx-redis   redis:7-alpine   "docker-entrypoint..."   Up   0.0.0.0:6379->6379/tcp
   ```

4. **Test Redis Connection**
   ```bash
   docker exec -it mentorx-redis redis-cli ping
   ```
   
   Should return: `PONG`

5. **Restart Your Spring Boot Application**

---

## Solution 2: Disable Redis Temporarily (Quick Fix)

If you don't need caching right now, you can disable Redis:

### Option A: Use Spring Profile Without Redis

Create `application-noredis.yml`:

```yaml
spring:
  cache:
    type: none
  data:
    redis:
      enabled: false
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
```

Run with profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=noredis
```

### Option B: Comment Out Redis Configuration

**File**: `src/main/java/com/mentorx/api/common/config/RedisConfig.java`

Add `@Profile("redis")` to disable by default:

```java
@Configuration
@EnableCaching
@Profile("redis")  // Add this line
public class RedisConfig {
    // ... rest of the code
}
```

Then Redis will only be enabled when you explicitly activate the "redis" profile.

### Option C: Use Simple Cache Instead

**File**: `application.yml` or `application.properties`

```yaml
spring:
  cache:
    type: simple  # Use in-memory cache instead of Redis
```

---

## Solution 3: Install Redis Locally (Without Docker)

### Windows
1. Download Redis for Windows from: https://github.com/microsoftarchive/redis/releases
2. Extract and run `redis-server.exe`
3. Redis will start on `localhost:6379`

### Linux/Mac
```bash
# Ubuntu/Debian
sudo apt-get install redis-server
sudo systemctl start redis

# Mac (Homebrew)
brew install redis
brew services start redis
```

---

## Recommended Approach

**For Development**: Use Docker (Solution 1)
- Consistent environment
- Easy to start/stop
- Matches production setup
- Already configured in docker-compose.yml

**For Quick Testing**: Disable Redis (Solution 2, Option B)
- Fastest to implement
- No external dependencies
- Good for testing non-cache features

---

## Verify Redis Configuration

**File**: `application.yml`

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:  # Leave empty if no password
      database: 0

cache:
  feed:
    ttl-hours: 1  # Cache TTL for personalized feed
```

---

## What Uses Redis in MentorX?

1. **Personalized Feed Caching** (`@Cacheable("personalizedFeed")`)
   - Caches mentor recommendations
   - Caches course recommendations
   - Caches job recommendations
   - TTL: 1 hour (configurable)

2. **Performance Benefits**
   - Reduces database queries
   - Faster response times for repeated requests
   - Scales better under load

---

## Quick Commands Reference

```bash
# Start Redis with Docker
docker-compose up -d redis

# Stop Redis
docker-compose stop redis

# View Redis logs
docker logs mentorx-redis

# Connect to Redis CLI
docker exec -it mentorx-redis redis-cli

# Check Redis keys
docker exec -it mentorx-redis redis-cli KEYS "*"

# Clear all Redis cache
docker exec -it mentorx-redis redis-cli FLUSHALL

# Restart Redis
docker-compose restart redis
```

---

## Next Steps

1. **Choose a solution** based on your needs
2. **Start Redis** (if using Solution 1)
3. **Restart your Spring Boot application**
4. **Test the personalized feed endpoints**

The application will work without Redis, but caching will be disabled and performance may be slower for repeated requests.
