package com.mentorx.api.feature.matching.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.matching.dto.request.UserInterestProfileRequest;
import com.mentorx.api.feature.matching.dto.response.UserInterestProfileResponse;
import com.mentorx.api.feature.matching.service.UserInterestProfileService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matching/user-interest-profiles")
@RequiredArgsConstructor
@Tag(name = "User Interest Profiles", description = "APIs for managing user interest profiles")
public class UserInterestProfileController {

    private final UserInterestProfileService userInterestProfileService;

    @PostMapping
    @Operation(summary = "Create user interest profile", description = "Create a new user interest profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInterestProfileResponse>> create(
            @Valid @RequestBody UserInterestProfileRequest request) {
        UserInterestProfileResponse response = userInterestProfileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User interest profile created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user interest profile by ID", description = "Retrieve user interest profile by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInterestProfileResponse>> getById(
            @Parameter(description = "Interest profile ID") @PathVariable UUID id) {
        UserInterestProfileResponse response = userInterestProfileService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user interest profile", description = "Update an existing user interest profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInterestProfileResponse>> update(
            @Parameter(description = "Interest profile ID") @PathVariable UUID id,
            @Valid @RequestBody UserInterestProfileRequest request) {
        UserInterestProfileResponse response = userInterestProfileService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("User interest profile updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user interest profile", description = "Delete a user interest profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Interest profile ID") @PathVariable UUID id) {
        userInterestProfileService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("User interest profile deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all user interest profiles", description = "Retrieve paginated list of all user interest profiles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<UserInterestProfileResponse>>> getAll(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "interestScore") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserInterestProfileResponse> response = userInterestProfileService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get interest profiles by user", description = "Retrieve all interest profiles for a specific user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserInterestProfileResponse>>> getByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<UserInterestProfileResponse> response = userInterestProfileService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/top")
    @Operation(summary = "Get top interests for user", description = "Retrieve top N interests for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserInterestProfileResponse>>> getTopInterestsForUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Number of results") @RequestParam(defaultValue = "5") int limit) {
        List<UserInterestProfileResponse> response = userInterestProfileService.getTopInterestsForUser(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/record-interaction")
    @Operation(summary = "Record user interaction", description = "Record a user interaction with a category")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> recordInteraction(
            @Parameter(description = "User ID") @RequestParam UUID userId,
            @Parameter(description = "Category ID") @RequestParam Integer categoryId,
            @Parameter(description = "Time spent in minutes") @RequestParam(defaultValue = "1") int timeSpentMinutes) {
        userInterestProfileService.recordInteraction(userId, categoryId, timeSpentMinutes);
        return ResponseEntity.ok(ApiResponse.success("Interaction recorded successfully", null));
    }

    @PostMapping("/user/{userId}/apply-decay")
    @Operation(summary = "Apply decay to interests", description = "Apply decay factor to all user interests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Void>> applyDecay(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        userInterestProfileService.applyDecay(userId);
        return ResponseEntity.ok(ApiResponse.success("Decay applied successfully", null));
    }
}
