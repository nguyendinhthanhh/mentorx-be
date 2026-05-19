package com.mentorx.api.feature.feed.job;

import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.feature.feed.service.FeedOrchestrationService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Background job for daily feed precomputation
 * Recalculates personalized feed items for all active users
 * Runs daily at 2 AM to ensure fresh recommendations
 * 
 * Requirements:
 * - 9.1: Run daily to recalculate all user feed items
 * - 9.2: Update precomputed_feed_items table with fresh recommendations
 * - 9.3: Invalidate all cached feeds in Redis after completion
 * - 9.4: Complete processing within 2 hours
 * - 9.5: Log errors and send admin alerts for job failures
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedPrecomputationJob {

    private final FeedOrchestrationService feedOrchestrationService;
    private final UserRepository userRepository;

    private static final int BATCH_SIZE = 100;
    private static final int LOG_INTERVAL = 100;

    /**
     * Daily feed recalculation job
     * Runs at 2 AM every day (cron: "0 0 2 * * *")
     * 
     * Requirement 9.1: Run daily to recalculate all user feed items
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void recalculateAllFeeds() {
        log.info("========================================");
        log.info("Starting daily feed precomputation job");
        log.info("========================================");

        LocalDateTime startTime = LocalDateTime.now();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger totalUsers = new AtomicInteger(0);

        try {
            // Get all active users
            // Requirement 9.2: Update precomputed_feed_items table for all active users
            List<User> activeUsers = userRepository.findByStatusAndDeletedAtIsNull(
                UserStatus.ACTIVE, 
                org.springframework.data.domain.Pageable.unpaged()
            ).getContent();

            totalUsers.set(activeUsers.size());
            log.info("Found {} active users to process", totalUsers.get());

            if (activeUsers.isEmpty()) {
                log.info("No active users found. Job completed.");
                return;
            }

            // Process each user
            for (int i = 0; i < activeUsers.size(); i++) {
                User user = activeUsers.get(i);
                UUID userId = user.getId();

                try {
                    // Precompute feed for this user
                    // This will:
                    // 1. Delete existing precomputed items
                    // 2. Compute fresh recommendations
                    // 3. Store in precomputed_feed_items table
                    // 4. Invalidate user's cache
                    feedOrchestrationService.precomputeFeedForUser(userId);
                    
                    successCount.incrementAndGet();

                    // Log progress periodically (every 100 users)
                    if ((i + 1) % LOG_INTERVAL == 0) {
                        log.info("Progress: {}/{} users processed ({} successful, {} failed)", 
                                 i + 1, totalUsers.get(), successCount.get(), failureCount.get());
                    }

                } catch (Exception e) {
                    // Don't let one user's failure stop the entire job
                    failureCount.incrementAndGet();
                    log.error("Failed to precompute feed for user {}: {}", userId, e.getMessage(), e);
                    
                    // Continue processing other users
                }
            }

            // Invalidate all cached feeds after processing all users
            // Requirement 9.3: Invalidate all cached feeds in Redis after completion
            try {
                feedOrchestrationService.invalidateAllFeeds();
                log.info("Successfully invalidated all feed caches");
            } catch (Exception e) {
                log.error("Failed to invalidate all feed caches: {}", e.getMessage(), e);
                // This is not critical - caches will expire naturally
            }

            // Calculate job duration
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);
            long durationMinutes = duration.toMinutes();
            long durationSeconds = duration.getSeconds() % 60;

            // Log final summary
            log.info("========================================");
            log.info("Feed precomputation job completed");
            log.info("Total users: {}", totalUsers.get());
            log.info("Successful: {}", successCount.get());
            log.info("Failed: {}", failureCount.get());
            log.info("Duration: {} minutes {} seconds", durationMinutes, durationSeconds);
            log.info("========================================");

            // Check if job completed within 2 hours (Requirement 9.4)
            if (duration.toHours() >= 2) {
                log.warn("WARNING: Feed precomputation job took longer than 2 hours! Duration: {} hours", 
                         duration.toHours());
                // TODO: Send admin alert - email/notification service doesn't exist yet
                // Requirement 9.5: Send admin alerts for job failures
            }

            // Check if there were significant failures
            if (failureCount.get() > 0) {
                double failureRate = (double) failureCount.get() / totalUsers.get() * 100;
                log.warn("Job completed with {} failures ({:.2f}% failure rate)", 
                         failureCount.get(), failureRate);
                
                if (failureRate > 10) {
                    log.error("CRITICAL: Feed precomputation job had high failure rate: {:.2f}%", failureRate);
                    // TODO: Send admin alert - email/notification service doesn't exist yet
                    // Requirement 9.5: Send admin alerts for job failures
                }
            }

        } catch (Exception e) {
            // Fatal error - log and alert
            // Requirement 9.5: Log errors and send admin alerts for job failures
            log.error("FATAL: Feed precomputation job failed with critical error", e);
            log.error("Error message: {}", e.getMessage());
            log.error("Processed {} users successfully before failure", successCount.get());
            
            // TODO: Send admin alert - email/notification service doesn't exist yet
            // This should trigger immediate notification to system administrators
            
            // Re-throw to ensure the error is visible in monitoring systems
            throw new RuntimeException("Feed precomputation job failed", e);
        }
    }

    /**
     * Manual trigger for feed recalculation (for testing/admin use)
     * Can be called via admin endpoint if needed
     */
    public void triggerManualRecalculation() {
        log.info("Manual feed recalculation triggered");
        recalculateAllFeeds();
    }
}
