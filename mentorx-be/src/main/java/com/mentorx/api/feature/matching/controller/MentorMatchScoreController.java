package com.mentorx.api.feature.matching.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.matching.dto.request.MentorMatchScoreRequest;
import com.mentorx.api.feature.matching.dto.response.MentorMatchScoreResponse;
import com.mentorx.api.feature.matching.service.MentorMatchScoreService;
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
@RequestMapping("/api/matching/mentor-match-scores")
@RequiredArgsConstructor
@Tag(name = "Mentor Match Scores", description = "APIs for managing mentor-user match scores")
public class MentorMatchScoreController {

    private final MentorMatchScoreService mentorMatchScoreService;

    @PostMapping
    @Operation(summary = "Create mentor match score", description = "Create a new mentor match score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<MentorMatchScoreResponse>> create(
            @Valid @RequestBody MentorMatchScoreRequest request) {
        MentorMatchScoreResponse response = mentorMatchScoreService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mentor match score created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mentor match score by ID", description = "Retrieve mentor match score by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorMatchScoreResponse>> getById(
            @Parameter(description = "Match score ID") @PathVariable UUID id) {
        MentorMatchScoreResponse response = mentorMatchScoreService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update mentor match score", description = "Update an existing mentor match score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<MentorMatchScoreResponse>> update(
            @Parameter(description = "Match score ID") @PathVariable UUID id,
            @Valid @RequestBody MentorMatchScoreRequest request) {
        MentorMatchScoreResponse response = mentorMatchScoreService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Mentor match score updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mentor match score", description = "Delete a mentor match score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Match score ID") @PathVariable UUID id) {
        mentorMatchScoreService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Mentor match score deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all mentor match scores", description = "Retrieve paginated list of all mentor match scores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<MentorMatchScoreResponse>>> getAll(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "matchScore") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MentorMatchScoreResponse> response = mentorMatchScoreService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get match scores by user", description = "Retrieve match scores for a specific user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MentorMatchScoreResponse>>> getByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchScore"));
        Page<MentorMatchScoreResponse> response = mentorMatchScoreService.getByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mentor/{mentorProfileId}")
    @Operation(summary = "Get match scores by mentor", description = "Retrieve match scores for a specific mentor")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MentorMatchScoreResponse>>> getByMentorProfileId(
            @Parameter(description = "Mentor profile ID") @PathVariable UUID mentorProfileId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchScore"));
        Page<MentorMatchScoreResponse> response = mentorMatchScoreService.getByMentorProfileId(mentorProfileId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/top")
    @Operation(summary = "Get top matches for user", description = "Retrieve top N mentor matches for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MentorMatchScoreResponse>>> getTopMatchesForUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Number of results") @RequestParam(defaultValue = "10") int limit) {
        
        List<MentorMatchScoreResponse> response = mentorMatchScoreService.getTopMatchesForUser(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/mark-shown")
    @Operation(summary = "Mark match as shown", description = "Mark a match score as shown to the user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsShown(
            @Parameter(description = "Match score ID") @PathVariable UUID id) {
        mentorMatchScoreService.markAsShown(id);
        return ResponseEntity.ok(ApiResponse.success("Match score marked as shown", null));
    }

    @PostMapping("/recompute-expired")
    @Operation(summary = "Recompute expired scores", description = "Recompute all expired match scores")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> recomputeExpiredScores() {
        mentorMatchScoreService.recomputeExpiredScores();
        return ResponseEntity.ok(ApiResponse.success("Expired scores recomputed successfully", null));
    }

    @PostMapping("/compute")
    @Operation(summary = "Compute match score", description = "Compute match score for a user and mentor")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Void>> computeMatchScore(
            @Parameter(description = "User ID") @RequestParam UUID userId,
            @Parameter(description = "Mentor profile ID") @RequestParam UUID mentorProfileId) {
        mentorMatchScoreService.computeMatchScore(userId, mentorProfileId);
        return ResponseEntity.ok(ApiResponse.success("Match score computed successfully", null));
    }
}
