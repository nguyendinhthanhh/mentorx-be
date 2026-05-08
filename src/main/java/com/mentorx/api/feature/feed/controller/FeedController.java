package com.mentorx.api.feature.feed.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.feed.dto.response.CourseRecommendationResponse;
import com.mentorx.api.feature.feed.dto.response.JobRecommendationResponse;
import com.mentorx.api.feature.feed.dto.response.MentorRecommendationResponse;
import com.mentorx.api.feature.feed.service.CourseRecommendationService;
import com.mentorx.api.feature.feed.service.JobRecommendationService;
import com.mentorx.api.feature.feed.service.MentorRecommendationService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controller for personalized feed recommendation endpoints
 * Provides access to mentor, course, knowledge, and job recommendations
 * 
 * @author MentorX Development Team
 * @since 2.2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "APIs for personalized content recommendations")
public class FeedController {

    private final MentorRecommendationService mentorRecommendationService;
    private final CourseRecommendationService courseRecommendationService;
    private final JobRecommendationService jobRecommendationService;
    private final UserRepository userRepository;

    /**
     * Get personalized mentor recommendations for the authenticated user
     * Returns mentors with match scores >= 85%, sorted by match score descending
     * For unauthenticated users, returns empty list (fallback to general API on frontend)
     * 
     * Requirements: 11.5, 11.9, 11.10
     */
    @GetMapping("/mentors")
    @Operation(
        summary = "Get personalized mentor recommendations",
        description = "Returns mentor recommendations based on user interests and skills with match scores >= 85%"
    )
    public ResponseEntity<ApiResponse<List<MentorRecommendationResponse>>> getMentorRecommendations(
            @Parameter(description = "Maximum number of recommendations to return (default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                log.info("Unauthenticated request for mentor recommendations - returning empty list");
                return ResponseEntity.ok(
                    ApiResponse.success("Mentor recommendations retrieved successfully (unauthenticated)", new ArrayList<>())
                );
            }
            
            log.info("Getting mentor recommendations for authenticated user with limit: {}", limit);
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // Get mentor recommendations from service
            List<MentorRecommendationResponse> mentors = mentorRecommendationService
                .getRecommendedMentors(currentUser.getId(), limit);
            
            log.info("Successfully retrieved {} mentor recommendations for user: {}", 
                     mentors.size(), currentUser.getId());
            
            return ResponseEntity.ok(
                ApiResponse.success("Mentor recommendations retrieved successfully", mentors)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving mentor recommendations", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve mentor recommendations: " + e.getMessage()));
        }
    }

    /**
     * Get personalized course recommendations for the authenticated user
     * Returns courses with match scores >= 85%, filtered by skill level and interest categories
     * For unauthenticated users, returns empty list (fallback to general API on frontend)
     * 
     * Requirements: 11.6, 11.9, 11.10
     */
    @GetMapping("/courses")
    @Operation(
        summary = "Get personalized course recommendations",
        description = "Returns course recommendations based on user skill level and interest categories with match scores >= 85%"
    )
    public ResponseEntity<ApiResponse<List<CourseRecommendationResponse>>> getCourseRecommendations(
            @Parameter(description = "Maximum number of recommendations to return (default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                log.info("Unauthenticated request for course recommendations - returning empty list");
                return ResponseEntity.ok(
                    ApiResponse.success("Course recommendations retrieved successfully (unauthenticated)", new ArrayList<>())
                );
            }
            
            log.info("Getting course recommendations for authenticated user with limit: {}", limit);
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // Get course recommendations from service
            List<CourseRecommendationResponse> courses = courseRecommendationService
                .getRecommendedCourses(currentUser.getId(), limit);
            
            log.info("Successfully retrieved {} course recommendations for user: {}", 
                     courses.size(), currentUser.getId());
            
            return ResponseEntity.ok(
                ApiResponse.success("Course recommendations retrieved successfully", courses)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving course recommendations", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve course recommendations: " + e.getMessage()));
        }
    }

    /**
     * Get personalized knowledge content recommendations for the authenticated user
     * 
     * TODO: Implement KnowledgeRecommendationService
     * Currently returns empty list - needs Knowledge/Article entity and service implementation
     * The knowledge/article entity is not yet in the database schema
     * 
     * Requirements: 11.7, 11.9, 11.10
     */
    @GetMapping("/knowledge")
    @Operation(
        summary = "Get personalized knowledge content recommendations",
        description = "Returns article and post recommendations based on user skill level and interests with match scores >= 85%"
    )
    public ResponseEntity<ApiResponse<List<Object>>> getKnowledgeRecommendations(
            @Parameter(description = "Maximum number of recommendations to return (default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                log.info("Unauthenticated request for knowledge recommendations - returning empty list");
                return ResponseEntity.ok(
                    ApiResponse.success("Knowledge recommendations retrieved successfully (unauthenticated)", new ArrayList<>())
                );
            }
            
            log.info("Getting knowledge recommendations for authenticated user with limit: {}", limit);
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // TODO: Replace with actual KnowledgeRecommendationService call
            // For now, return empty list as Knowledge/Article entity doesn't exist yet
            List<Object> knowledge = new ArrayList<>();
            
            log.warn("Knowledge recommendations not yet implemented - returning empty list for user: {}", 
                     currentUser.getId());
            
            return ResponseEntity.ok(
                ApiResponse.success("Knowledge recommendations retrieved successfully (placeholder)", knowledge)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving knowledge recommendations", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve knowledge recommendations: " + e.getMessage()));
        }
    }

    /**
     * Get personalized job recommendations for the authenticated user
     * Returns jobs with match scores >= 85%, filtered by user skills and appropriate budget range
     * For unauthenticated users, returns empty list (fallback to general API on frontend)
     * 
     * Requirements: 11.8, 11.9, 11.10
     */
    @GetMapping("/jobs")
    @Operation(
        summary = "Get personalized job recommendations",
        description = "Returns job recommendations based on user skills and budget range with match scores >= 85%"
    )
    public ResponseEntity<ApiResponse<List<JobRecommendationResponse>>> getJobRecommendations(
            @Parameter(description = "Maximum number of recommendations to return (default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                log.info("Unauthenticated request for job recommendations - returning empty list");
                return ResponseEntity.ok(
                    ApiResponse.success("Job recommendations retrieved successfully (unauthenticated)", new ArrayList<>())
                );
            }
            
            log.info("Getting job recommendations for authenticated user with limit: {}", limit);
            
            // Get current authenticated user
            User currentUser = getCurrentUser();
            
            // Get job recommendations from service
            List<JobRecommendationResponse> jobs = jobRecommendationService
                .getRecommendedJobs(currentUser.getId(), limit);
            
            log.info("Successfully retrieved {} job recommendations for user: {}", 
                     jobs.size(), currentUser.getId());
            
            return ResponseEntity.ok(
                ApiResponse.success("Job recommendations retrieved successfully", jobs)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving job recommendations", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve job recommendations: " + e.getMessage()));
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
