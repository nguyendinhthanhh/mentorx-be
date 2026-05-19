package com.mentorx.api.feature.mentor.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.mentor.dto.request.MentorAvailabilityRequest;
import com.mentorx.api.feature.mentor.dto.request.MentorBlockedDateRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorAvailabilityResponse;
import com.mentorx.api.feature.mentor.dto.response.MentorBlockedDateResponse;
import com.mentorx.api.feature.mentor.dto.response.WeeklyAvailabilityResponse;
import com.mentorx.api.feature.mentor.service.MentorAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@Tag(name = "Mentor Availability", description = "APIs for managing mentor availability and blocked dates")
public class MentorAvailabilityController {

    private final MentorAvailabilityService mentorAvailabilityService;

    @PostMapping("/{userId}/availability")
    @Operation(summary = "Create availability slot", description = "Create a new availability slot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorAvailabilityResponse>> createAvailability(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody MentorAvailabilityRequest request) {
        MentorAvailabilityResponse response = mentorAvailabilityService.createAvailability(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability created successfully", response));
    }

    @PutMapping("/availability/{availabilityId}")
    @Operation(summary = "Update availability slot", description = "Update an existing availability slot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorAvailabilityResponse>> updateAvailability(
            @Parameter(description = "Availability ID") @PathVariable UUID availabilityId,
            @Valid @RequestBody MentorAvailabilityRequest request) {
        MentorAvailabilityResponse response = mentorAvailabilityService.updateAvailability(availabilityId, request);
        return ResponseEntity.ok(ApiResponse.success("Availability updated successfully", response));
    }

    @DeleteMapping("/availability/{availabilityId}")
    @Operation(summary = "Delete availability slot", description = "Delete an availability slot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(
            @Parameter(description = "Availability ID") @PathVariable UUID availabilityId) {
        mentorAvailabilityService.deleteAvailability(availabilityId);
        return ResponseEntity.ok(ApiResponse.success("Availability deleted successfully", null));
    }

    @GetMapping("/availability/{availabilityId}")
    @Operation(summary = "Get availability by ID", description = "Get availability details by ID")
    public ResponseEntity<ApiResponse<MentorAvailabilityResponse>> getAvailabilityById(
            @Parameter(description = "Availability ID") @PathVariable UUID availabilityId) {
        MentorAvailabilityResponse response = mentorAvailabilityService.getAvailabilityById(availabilityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/availability")
    @Operation(summary = "Get all availability for mentor", description = "Get all availability slots for a specific mentor")
    public ResponseEntity<ApiResponse<List<MentorAvailabilityResponse>>> getAllAvailabilityByMentor(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<MentorAvailabilityResponse> response = mentorAvailabilityService.getAllAvailabilityByMentor(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/availability/week")
    @Operation(summary = "Get weekly availability", description = "Get aggregated weekly availability schedule")
    public ResponseEntity<ApiResponse<WeeklyAvailabilityResponse>> getWeeklyAvailability(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        WeeklyAvailabilityResponse response = mentorAvailabilityService.getWeeklyAvailability(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{userId}/blocked-dates")
    @Operation(summary = "Block a date", description = "Block a specific date for the mentor")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorBlockedDateResponse>> blockDate(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody MentorBlockedDateRequest request) {
        MentorBlockedDateResponse response = mentorAvailabilityService.blockDate(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Date blocked successfully", response));
    }

    @DeleteMapping("/blocked-dates/{blockedDateId}")
    @Operation(summary = "Unblock a date", description = "Remove a blocked date")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> unblockDate(
            @Parameter(description = "Blocked Date ID") @PathVariable UUID blockedDateId) {
        mentorAvailabilityService.unblockDate(blockedDateId);
        return ResponseEntity.ok(ApiResponse.success("Date unblocked successfully", null));
    }

    @GetMapping("/{userId}/blocked-dates")
    @Operation(summary = "Get blocked dates for mentor", description = "Get all blocked dates for a specific mentor")
    public ResponseEntity<ApiResponse<List<MentorBlockedDateResponse>>> getBlockedDatesByMentor(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<MentorBlockedDateResponse> response = mentorAvailabilityService.getBlockedDatesByMentor(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
