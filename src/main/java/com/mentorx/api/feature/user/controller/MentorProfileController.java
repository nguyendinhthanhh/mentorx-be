package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.user.dto.request.MentorProfileRequest;
import com.mentorx.api.feature.user.dto.response.MentorProfileResponse;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.service.MentorProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@Tag(name = "Mentor Profile Management", description = "APIs for mentor profile operations")
public class MentorProfileController {

    private final MentorProfileService mentorProfileService;
    private final MentorModeAccessService mentorModeAccessService;

    @PostMapping("/{userId}/profile")
    @Operation(summary = "Create mentor profile", description = "Create a mentor profile for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> createMentorProfile(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody MentorProfileRequest request) {
        MentorProfileResponse profile = mentorProfileService.createMentorProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mentor profile created successfully", profile));
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply to become a mentor", description = "Create or submit the current user's mentor application")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> applyForMentor(
            @Valid @RequestBody MentorProfileRequest request) {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        MentorProfileResponse profile = mentorProfileService.createMentorProfile(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mentor application submitted successfully", profile));
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get mentor profile", description = "Get mentor profile by user ID")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getMentorProfile(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        MentorProfileResponse profile = mentorProfileService.getMentorProfileByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/{userId}/profile")
    @Operation(summary = "Update mentor profile", description = "Update mentor profile information")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> updateMentorProfile(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody MentorProfileRequest request) {
        MentorProfileResponse profile = mentorProfileService.updateMentorProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Mentor profile updated successfully", profile));
    }

    @DeleteMapping("/{userId}/profile")
    @Operation(summary = "Delete mentor profile", description = "Delete mentor profile")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteMentorProfile(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        mentorProfileService.deleteMentorProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Mentor profile deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all approved mentors", description = "Get paginated list of approved mentors")
    public ResponseEntity<ApiResponse<Page<MentorProfileResponse>>> getAllApprovedMentors(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "averageRating") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MentorProfileResponse> mentors = mentorProfileService.getAllApprovedMentors(pageable);
        return ResponseEntity.ok(ApiResponse.success(mentors));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending mentor applications", description = "Get paginated list of pending mentor applications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<MentorProfileResponse>>> getPendingApplications(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<MentorProfileResponse> applications = mentorProfileService.getPendingMentorApplications(pageable);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @GetMapping("/application/status")
    @Operation(summary = "Get current mentor application status", description = "Get the authenticated user's mentor application state")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentApplicationStatus() {
        UUID currentUserId = mentorModeAccessService.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(mentorProfileService.getCurrentApplicationStatus(currentUserId)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search mentors", description = "Search mentors with filters")
    public ResponseEntity<ApiResponse<Page<MentorProfileResponse>>> searchMentors(
            @Parameter(description = "Minimum rating") @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "Maximum hourly rate") @RequestParam(required = false) BigDecimal maxHourlyRate,
            @Parameter(description = "Availability") @RequestParam(required = false) String availability,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "averageRating") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MentorProfileResponse> mentors = mentorProfileService.getMentorsWithFilters(
                minRating, maxHourlyRate, availability, pageable);
        return ResponseEntity.ok(ApiResponse.success(mentors));
    }

    @GetMapping("/search/text")
    @Operation(summary = "Full-text search mentors", description = "Full-text search for mentors")
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> searchMentorsFullText(
            @Parameter(description = "Search query") @RequestParam String query) {
        List<MentorProfileResponse> mentors = mentorProfileService.searchMentors(query);
        return ResponseEntity.ok(ApiResponse.success(mentors));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured mentors", description = "Get list of featured mentors")
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> getFeaturedMentors() {
        List<MentorProfileResponse> mentors = mentorProfileService.getFeaturedMentors();
        return ResponseEntity.ok(ApiResponse.success(mentors));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top-rated mentors", description = "Get paginated list of top-rated mentors")
    public ResponseEntity<ApiResponse<Page<MentorProfileResponse>>> getTopRatedMentors(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MentorProfileResponse> mentors = mentorProfileService.getTopRatedMentors(pageable);
        return ResponseEntity.ok(ApiResponse.success(mentors));
    }

    @GetMapping("/saved")
    @Operation(summary = "Get saved mentors", description = "Get mentors saved by a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> getSavedMentors(
            @Parameter(description = "User ID") @RequestParam UUID userId) {
        List<MentorProfileResponse> mentors = mentorProfileService.getSavedMentors(userId);
        return ResponseEntity.ok(ApiResponse.success(mentors));
    }

    @GetMapping("/{mentorUserId}/save-status")
    @Operation(summary = "Check saved mentor status", description = "Check whether a user has saved a mentor")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> isMentorSaved(
            @Parameter(description = "Mentor user ID") @PathVariable UUID mentorUserId,
            @Parameter(description = "User ID") @RequestParam UUID userId) {
        boolean saved = mentorProfileService.isMentorSaved(userId, mentorUserId);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PostMapping("/{mentorUserId}/save")
    @Operation(summary = "Save mentor", description = "Save a mentor for the current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> saveMentor(
            @Parameter(description = "Mentor user ID") @PathVariable UUID mentorUserId,
            @Parameter(description = "User ID") @RequestParam UUID userId) {
        boolean saved = mentorProfileService.saveMentor(userId, mentorUserId);
        return ResponseEntity.ok(ApiResponse.success("Mentor saved", saved));
    }

    @DeleteMapping("/{mentorUserId}/save")
    @Operation(summary = "Unsave mentor", description = "Remove a mentor from saved mentors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> unsaveMentor(
            @Parameter(description = "Mentor user ID") @PathVariable UUID mentorUserId,
            @Parameter(description = "User ID") @RequestParam UUID userId) {
        boolean saved = mentorProfileService.unsaveMentor(userId, mentorUserId);
        return ResponseEntity.ok(ApiResponse.success("Mentor removed from saved list", saved));
    }

    @PostMapping("/{userId}/approve")
    @Operation(summary = "Approve mentor application", description = "Approve a pending mentor application")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> approveMentorApplication(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Approver ID") @RequestParam(required = false) UUID approvedBy) {
        UUID approverId = approvedBy != null ? approvedBy : mentorModeAccessService.getCurrentUserId();
        MentorProfileResponse profile = mentorProfileService.approveMentorApplication(userId, approverId);
        return ResponseEntity.ok(ApiResponse.success("Mentor application approved", profile));
    }

    @PostMapping("/{userId}/reject")
    @Operation(summary = "Reject mentor application", description = "Reject a pending mentor application")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> rejectMentorApplication(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Rejection reason") @RequestParam String reason,
            @Parameter(description = "Rejector ID") @RequestParam(required = false) UUID rejectedBy) {
        UUID rejectorId = rejectedBy != null ? rejectedBy : mentorModeAccessService.getCurrentUserId();
        MentorProfileResponse profile = mentorProfileService.rejectMentorApplication(userId, reason, rejectorId);
        return ResponseEntity.ok(ApiResponse.success("Mentor application rejected", profile));
    }

    @PostMapping("/{userId}/request-revision")
    @Operation(summary = "Request mentor application revision", description = "Ask a user to supplement or correct mentor application information")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> requestMentorApplicationRevision(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Revision reason") @RequestParam String reason,
            @Parameter(description = "Reviewer ID") @RequestParam(required = false) UUID requestedBy) {
        UUID reviewerId = requestedBy != null ? requestedBy : mentorModeAccessService.getCurrentUserId();
        MentorProfileResponse profile = mentorProfileService.requestMentorApplicationRevision(userId, reason, reviewerId);
        return ResponseEntity.ok(ApiResponse.success("Mentor application revision requested", profile));
    }

    @PostMapping("/{userId}/suspend")
    @Operation(summary = "Suspend mentor access", description = "Suspend an approved mentor from Mentor Mode")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> suspendMentor(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Suspension reason") @RequestParam String reason,
            @Parameter(description = "Moderator ID") @RequestParam(required = false) UUID suspendedBy) {
        UUID moderatorId = suspendedBy != null ? suspendedBy : mentorModeAccessService.getCurrentUserId();
        MentorProfileResponse profile = mentorProfileService.suspendMentor(userId, reason, moderatorId);
        return ResponseEntity.ok(ApiResponse.success("Mentor suspended", profile));
    }

    @PatchMapping("/{userId}/featured")
    @Operation(summary = "Set featured status", description = "Set mentor featured status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> setFeaturedStatus(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Featured status") @RequestParam boolean featured) {
        mentorProfileService.setFeaturedStatus(userId, featured);
        return ResponseEntity.ok(ApiResponse.success("Featured status updated", null));
    }

    @GetMapping("/statistics/approved")
    @Operation(summary = "Get approved mentors count", description = "Get number of approved mentors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Long>> getApprovedMentorsCount() {
        long count = mentorProfileService.getApprovedMentorsCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/statistics/pending")
    @Operation(summary = "Get pending applications count", description = "Get number of pending mentor applications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Long>> getPendingApplicationsCount() {
        long count = mentorProfileService.getPendingApplicationsCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
