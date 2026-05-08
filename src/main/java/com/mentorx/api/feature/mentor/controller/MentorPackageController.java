package com.mentorx.api.feature.mentor.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.mentor.dto.request.MentorPackageRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorPackageResponse;
import com.mentorx.api.feature.mentor.service.MentorPackageService;
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
@Tag(name = "Mentor Packages", description = "APIs for managing mentor packages")
public class MentorPackageController {

    private final MentorPackageService mentorPackageService;

    @PostMapping("/{userId}/packages")
    @Operation(summary = "Create mentor package", description = "Create a new mentoring package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorPackageResponse>> createPackage(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody MentorPackageRequest request) {
        MentorPackageResponse response = mentorPackageService.createPackage(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Package created successfully", response));
    }

    @PutMapping("/packages/{packageId}")
    @Operation(summary = "Update mentor package", description = "Update an existing mentoring package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorPackageResponse>> updatePackage(
            @Parameter(description = "Package ID") @PathVariable UUID packageId,
            @Valid @RequestBody MentorPackageRequest request) {
        MentorPackageResponse response = mentorPackageService.updatePackage(packageId, request);
        return ResponseEntity.ok(ApiResponse.success("Package updated successfully", response));
    }

    @DeleteMapping("/packages/{packageId}")
    @Operation(summary = "Delete mentor package", description = "Delete a mentoring package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @Parameter(description = "Package ID") @PathVariable UUID packageId) {
        mentorPackageService.deletePackage(packageId);
        return ResponseEntity.ok(ApiResponse.success("Package deleted successfully", null));
    }

    @PatchMapping("/packages/{packageId}/toggle")
    @Operation(summary = "Toggle package active status", description = "Toggle the active status of a package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorPackageResponse>> toggleActiveStatus(
            @Parameter(description = "Package ID") @PathVariable UUID packageId) {
        MentorPackageResponse response = mentorPackageService.toggleActiveStatus(packageId);
        return ResponseEntity.ok(ApiResponse.success("Package status toggled successfully", response));
    }

    @GetMapping("/packages/{packageId}")
    @Operation(summary = "Get package by ID", description = "Get package details by ID")
    public ResponseEntity<ApiResponse<MentorPackageResponse>> getPackageById(
            @Parameter(description = "Package ID") @PathVariable UUID packageId) {
        MentorPackageResponse response = mentorPackageService.getPackageById(packageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/packages")
    @Operation(summary = "Get all packages for mentor", description = "Get all packages for a specific mentor")
    public ResponseEntity<ApiResponse<List<MentorPackageResponse>>> getAllPackagesByMentor(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<MentorPackageResponse> response = mentorPackageService.getAllPackagesByMentor(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/packages/active")
    @Operation(summary = "Get active packages for mentor", description = "Get all active packages for a specific mentor")
    public ResponseEntity<ApiResponse<List<MentorPackageResponse>>> getActivePackagesByMentor(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<MentorPackageResponse> response = mentorPackageService.getActivePackagesByMentor(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
