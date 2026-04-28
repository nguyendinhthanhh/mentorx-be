package com.mentorx.api.feature.mentor.controller;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.mentor.dto.request.MentorApplicationRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorProfileResponse;
import com.mentorx.api.feature.mentor.service.MentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
@Tag(name = "Mentor Management", description = "APIs for mentor profile management")
public class MentorController {

    private final MentorService mentorService;

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Apply as mentor", description = "Submit mentor application")
    public ApiResponse<MentorProfileResponse> applyAsMentor(
            @Parameter(description = "User ID") @RequestParam UUID userId,
            @Valid @RequestBody MentorApplicationRequest request) {
        
        MentorProfileResponse response = mentorService.applyAsMentor(userId, request);
        return new ApiResponse<>(true, "Mentor application submitted successfully", response, LocalDateTime.now());
    }

    @GetMapping("/{mentorProfileId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get mentor profile", description = "Get mentor profile by ID")
    public ApiResponse<MentorProfileResponse> getMentorProfile(
            @Parameter(description = "Mentor profile ID") @PathVariable UUID mentorProfileId) {
        
        MentorProfileResponse response = mentorService.getMentorProfile(mentorProfileId);
        return new ApiResponse<>(true, "Mentor profile retrieved successfully", response, LocalDateTime.now());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get mentor profile by user ID", description = "Get mentor profile by user ID")
    public ApiResponse<MentorProfileResponse> getMentorProfileByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        MentorProfileResponse response = mentorService.getMentorProfileByUserId(userId);
        return new ApiResponse<>(true, "Mentor profile retrieved successfully", response, LocalDateTime.now());
    }

    @PutMapping("/{mentorProfileId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update mentor profile", description = "Update mentor profile")
    public ApiResponse<MentorProfileResponse> updateMentorProfile(
            @Parameter(description = "Mentor profile ID") @PathVariable UUID mentorProfileId,
            @Valid @RequestBody MentorApplicationRequest request) {
        
        MentorProfileResponse response = mentorService.updateMentorProfile(mentorProfileId, request);
        return new ApiResponse<>(true, "Mentor profile updated successfully", response, LocalDateTime.now());
    }

    @DeleteMapping("/{mentorProfileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete mentor profile", description = "Delete mentor profile")
    public ApiResponse<Void> deleteMentorProfile(
            @Parameter(description = "Mentor profile ID") @PathVariable UUID mentorProfileId) {
        
        mentorService.deleteMentorProfile(mentorProfileId);
        return new ApiResponse<>(true, "Mentor profile deleted successfully", null, LocalDateTime.now());
    }

    @PostMapping("/{mentorProfileId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve mentor application", description = "Approve mentor application")
    public ApiResponse<MentorProfileResponse> approveMentorApplication(
            @Parameter(description = "Mentor profile ID") @PathVariable UUID mentorProfileId,
            @Parameter(description = "Approved by user ID") @RequestParam UUID approvedBy,
            @Parameter(description = "Admin notes") @RequestParam(required = false) String adminNotes) {
        
        MentorProfileResponse response = mentorService.approveMentorApplication(mentorProfileId, approvedBy, adminNotes);
        return new ApiResponse<>(true, "Mentor application approved successfully", response, LocalDateTime.now());
    }

    @PostMapping("/{mentorProfileId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject mentor application", description = "Reject mentor application")
    public ApiResponse<MentorProfileResponse> rejectMentorApplication(
            @Parameter(description = "Mentor profile ID") @PathVariable UUID mentorProfileId,
            @Parameter(description = "Rejected by user ID") @RequestParam UUID rejectedBy,
            @Parameter(description = "Admin notes") @RequestParam(required = false) String adminNotes) {
        
        MentorProfileResponse response = mentorService.rejectMentorApplication(mentorProfileId, rejectedBy, adminNotes);
        return new ApiResponse<>(true, "Mentor application rejected successfully", response, LocalDateTime.now());
    }

    @PutMapping("/{mentorProfileId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update mentor status", description = "Update mentor status")
    public ApiResponse<MentorProfileResponse> updateMentorStatus(
            @Parameter(description = "Mentor profile ID") @PathVariable UUID mentorProfileId,
            @Parameter(description = "New status") @RequestParam MentorStatus status) {
        
        MentorProfileResponse response = mentorService.updateMentorStatus(mentorProfileId, status);
        return new ApiResponse<>(true, "Mentor status updated successfully", response, LocalDateTime.now());
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all mentors", description = "Get all mentors with pagination")
    public ApiResponse<Page<MentorProfileResponse>> getAllMentors(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<MentorProfileResponse> response = mentorService.getAllMentors(pageable);
        return new ApiResponse<>(true, "Mentors retrieved successfully", response, LocalDateTime.now());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get mentors by status", description = "Get mentors by status with pagination")
    public ApiResponse<Page<MentorProfileResponse>> getMentorsByStatus(
            @Parameter(description = "Mentor status") @PathVariable MentorStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<MentorProfileResponse> response = mentorService.getMentorsByStatus(status, pageable);
        return new ApiResponse<>(true, "Mentors retrieved successfully", response, LocalDateTime.now());
    }

    @GetMapping("/available/jobs")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get available mentors for jobs", description = "Get mentors available for jobs")
    public ApiResponse<Page<MentorProfileResponse>> getAvailableMentorsForJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<MentorProfileResponse> response = mentorService.getAvailableMentorsForJobs(pageable);
        return new ApiResponse<>(true, "Available mentors for jobs retrieved successfully", response, LocalDateTime.now());
    }

    @GetMapping("/available/courses")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get available mentors for courses", description = "Get mentors available for courses")
    public ApiResponse<Page<MentorProfileResponse>> getAvailableMentorsForCourses(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<MentorProfileResponse> response = mentorService.getAvailableMentorsForCourses(pageable);
        return new ApiResponse<>(true, "Available mentors for courses retrieved successfully", response, LocalDateTime.now());
    }

    @GetMapping("/search/skill")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search mentors by skill", description = "Search mentors by skill name")
    public ApiResponse<List<MentorProfileResponse>> searchMentorsBySkill(
            @Parameter(description = "Skill name") @RequestParam String skillName) {
        
        List<MentorProfileResponse> response = mentorService.searchMentorsBySkill(skillName);
        return new ApiResponse<>(true, "Mentors found by skill", response, LocalDateTime.now());
    }

    @GetMapping("/search/rate")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get mentors by hourly rate range", description = "Get mentors by hourly rate range")
    public ApiResponse<List<MentorProfileResponse>> getMentorsByHourlyRateRange(
            @Parameter(description = "Minimum rate") @RequestParam BigDecimal minRate,
            @Parameter(description = "Maximum rate") @RequestParam BigDecimal maxRate) {
        
        List<MentorProfileResponse> response = mentorService.getMentorsByHourlyRateRange(minRate, maxRate);
        return new ApiResponse<>(true, "Mentors found by rate range", response, LocalDateTime.now());
    }

    @GetMapping("/search/filters")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search mentors with filters", description = "Search mentors with multiple filters")
    public ApiResponse<Page<MentorProfileResponse>> getMentorsWithFilters(
            @Parameter(description = "Skill name") @RequestParam(required = false) String skillName,
            @Parameter(description = "Minimum rate") @RequestParam(required = false) BigDecimal minRate,
            @Parameter(description = "Maximum rate") @RequestParam(required = false) BigDecimal maxRate,
            @Parameter(description = "Minimum experience") @RequestParam(required = false) Integer minExperience,
            @Parameter(description = "Available for jobs") @RequestParam(required = false) Boolean availableForJobs,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<MentorProfileResponse> response = mentorService.getMentorsWithFilters(
                skillName, minRate, maxRate, minExperience, availableForJobs, pageable);
        return new ApiResponse<>(true, "Mentors found with filters", response, LocalDateTime.now());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search mentors", description = "Full-text search mentors")
    public ApiResponse<List<MentorProfileResponse>> searchMentors(
            @Parameter(description = "Search term") @RequestParam String searchTerm) {
        
        List<MentorProfileResponse> response = mentorService.searchMentors(searchTerm);
        return new ApiResponse<>(true, "Mentors found", response, LocalDateTime.now());
    }

    @GetMapping("/response-time/{maxResponseTime}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get mentors by max response time", description = "Get mentors by maximum response time")
    public ApiResponse<List<MentorProfileResponse>> getMentorsByMaxResponseTime(
            @Parameter(description = "Maximum response time in hours") @PathVariable Integer maxResponseTime) {
        
        List<MentorProfileResponse> response = mentorService.getMentorsByMaxResponseTime(maxResponseTime);
        return new ApiResponse<>(true, "Mentors found by response time", response, LocalDateTime.now());
    }

    @GetMapping("/stats/total")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get total mentors count", description = "Get total mentors count")
    public ApiResponse<Long> getTotalMentorsCount() {
        Long count = mentorService.getTotalMentorsCount();
        return new ApiResponse<>(true, "Total mentors count retrieved", count, LocalDateTime.now());
    }

    @GetMapping("/stats/approved")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get approved mentors count", description = "Get approved mentors count")
    public ApiResponse<Long> getApprovedMentorsCount() {
        Long count = mentorService.getApprovedMentorsCount();
        return new ApiResponse<>(true, "Approved mentors count retrieved", count, LocalDateTime.now());
    }

    @GetMapping("/stats/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending applications count", description = "Get pending applications count")
    public ApiResponse<Long> getPendingApplicationsCount() {
        Long count = mentorService.getPendingApplicationsCount();
        return new ApiResponse<>(true, "Pending applications count retrieved", count, LocalDateTime.now());
    }

    @GetMapping("/skills/popular")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get popular skills", description = "Get popular skills among mentors")
    public ApiResponse<List<String>> getPopularSkills() {
        List<String> skills = mentorService.getPopularSkills();
        return new ApiResponse<>(true, "Popular skills retrieved", skills, LocalDateTime.now());
    }

    @GetMapping("/skills/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search skills", description = "Search skills by name pattern")
    public ApiResponse<List<String>> searchSkills(
            @Parameter(description = "Skill name pattern") @RequestParam String skillName) {
        
        List<String> skills = mentorService.searchSkills(skillName);
        return new ApiResponse<>(true, "Skills found", skills, LocalDateTime.now());
    }

    @PutMapping("/{mentorProfileId}/availability")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update mentor availability", description = "Update mentor availability for jobs and courses")
    public ApiResponse<Void> updateMentorAvailability(
            @Parameter(description = "Mentor profile ID") @PathVariable UUID mentorProfileId,
            @Parameter(description = "Available for jobs") @RequestParam(required = false) Boolean availableForJobs,
            @Parameter(description = "Available for courses") @RequestParam(required = false) Boolean availableForCourses) {
        
        mentorService.updateMentorAvailability(mentorProfileId, availableForJobs, availableForCourses);
        return new ApiResponse<>(true, "Mentor availability updated successfully", null, LocalDateTime.now());
    }
}