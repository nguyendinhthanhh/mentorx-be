package com.mentorx.api.feature.dashboard.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.dashboard.dto.response.OnboardingProgressResponse;
import com.mentorx.api.feature.dashboard.dto.response.UserActivityResponse;
import com.mentorx.api.feature.dashboard.dto.response.WalletBalanceResponse;
import com.mentorx.api.feature.feed.dto.response.PersonalizedFeedResponse;
import com.mentorx.api.feature.feed.service.FeedOrchestrationService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Controller for personalized dashboard endpoints
 * Provides access to personalized feed, onboarding progress, wallet balance, and user activity
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for personalized discovery dashboard")
public class DashboardController {

    private final FeedOrchestrationService feedOrchestrationService;
    private final UserRepository userRepository;

    /**
     * Get personalized feed for the authenticated user
     * Returns mentor, course, and job recommendations based on user interests
     * 
     * Requirements: 11.1, 11.9, 11.10
     */
    @GetMapping("/dashboard/personalized")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get personalized dashboard feed",
        description = "Returns personalized recommendations for mentors, courses, and jobs based on user interests and skills"
    )
    public ResponseEntity<ApiResponse<PersonalizedFeedResponse>> getPersonalizedDashboard() {
        try {
            log.info("Getting personalized dashboard for authenticated user");
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // Get personalized feed from orchestration service
            PersonalizedFeedResponse feed = feedOrchestrationService.getPersonalizedFeed(currentUser.getId());
            
            log.info("Successfully retrieved personalized feed for user: {} with {} total items", 
                     currentUser.getId(), feed.getTotalItems());
            
            return ResponseEntity.ok(
                ApiResponse.success("Personalized dashboard retrieved successfully", feed)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving personalized dashboard", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve personalized dashboard: " + e.getMessage()));
        }
    }

    /**
     * Get onboarding progress for the authenticated user
     * 
     * TODO: Implement actual onboarding service integration
     * Currently returns mock data - needs OnboardingService implementation
     * 
     * Requirements: 11.2, 11.9, 11.10
     */
    @GetMapping("/onboarding/progress")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get onboarding progress",
        description = "Returns the user's onboarding completion status and current step"
    )
    public ResponseEntity<ApiResponse<OnboardingProgressResponse>> getOnboardingProgress() {
        try {
            log.info("Getting onboarding progress for authenticated user");
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // TODO: Replace with actual OnboardingService call
            // For now, return mock data based on user's isOnboarded flag
            OnboardingProgressResponse progress = OnboardingProgressResponse.builder()
                .isComplete(currentUser.getIsOnboarded())
                .completionPercentage(currentUser.getIsOnboarded() ? 100 : 60)
                .currentStep(currentUser.getIsOnboarded() ? "completed" : "interests")
                .totalSteps(5)
                .build();
            
            log.info("Retrieved onboarding progress for user: {} - {}% complete", 
                     currentUser.getId(), progress.getCompletionPercentage());
            
            return ResponseEntity.ok(
                ApiResponse.success("Onboarding progress retrieved successfully", progress)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving onboarding progress", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve onboarding progress: " + e.getMessage()));
        }
    }

    /**
     * Get wallet balance for the authenticated user
     * 
     * TODO: Implement actual wallet service integration
     * Currently returns mock data - needs WalletService implementation
     * 
     * Requirements: 11.3, 11.9, 11.10
     */
    @GetMapping("/wallet/balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get wallet balance",
        description = "Returns the user's current MX Credits balance"
    )
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getWalletBalance() {
        try {
            log.info("Getting wallet balance for authenticated user");
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // TODO: Replace with actual WalletService call
            // For now, return mock data
            WalletBalanceResponse balance = WalletBalanceResponse.builder()
                .balance(new BigDecimal("1000.00"))
                .currency("MXC")
                .pendingBalance(new BigDecimal("200.00"))
                .availableBalance(new BigDecimal("800.00"))
                .build();
            
            log.info("Retrieved wallet balance for user: {} - {} MXC", 
                     currentUser.getId(), balance.getBalance());
            
            return ResponseEntity.ok(
                ApiResponse.success("Wallet balance retrieved successfully", balance)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving wallet balance", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve wallet balance: " + e.getMessage()));
        }
    }

    /**
     * Get user activity summary for the authenticated user
     * 
     * TODO: Implement actual activity tracking service integration
     * Currently returns mock data - needs ActivityTrackerService implementation
     * 
     * Requirements: 11.4, 11.9, 11.10
     */
    @GetMapping("/user/activity")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user activity summary",
        description = "Returns the user's current learning activities and active contracts"
    )
    public ResponseEntity<ApiResponse<UserActivityResponse>> getUserActivity() {
        try {
            log.info("Getting user activity for authenticated user");
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // TODO: Replace with actual ActivityTrackerService call
            // For now, return mock data
            UserActivityResponse activity = UserActivityResponse.builder()
                .activeCourses(3)
                .activeContracts(2)
                .inProgressItems(5)
                .recentActivities(new ArrayList<>())
                .build();
            
            // Add some mock recent activities
            activity.getRecentActivities().add(
                UserActivityResponse.ActivityItem.builder()
                    .type("course")
                    .title("Java Spring Boot Masterclass")
                    .description("Completed lesson 5")
                    .timestamp(LocalDateTime.now().minusHours(2).toString())
                    .build()
            );
            
            activity.getRecentActivities().add(
                UserActivityResponse.ActivityItem.builder()
                    .type("contract")
                    .title("Build REST API")
                    .description("Milestone 1 approved")
                    .timestamp(LocalDateTime.now().minusHours(5).toString())
                    .build()
            );
            
            log.info("Retrieved user activity for user: {} - {} active courses, {} active contracts", 
                     currentUser.getId(), activity.getActiveCourses(), activity.getActiveContracts());
            
            return ResponseEntity.ok(
                ApiResponse.success("User activity retrieved successfully", activity)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving user activity", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve user activity: " + e.getMessage()));
        }
    }

    /**
     * Get current authenticated user from security context
     * 
     * @return Current authenticated user
     * @throws RuntimeException if user not found or not authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}
