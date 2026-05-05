package com.mentorx.api.feature.system.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.system.dto.request.NotificationPreferenceRequest;
import com.mentorx.api.feature.system.dto.response.NotificationPreferenceResponse;
import com.mentorx.api.feature.system.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/system/notification-preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "APIs for managing notification preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    @PostMapping
    @Operation(summary = "Create notification preference", description = "Create notification preference for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> create(
            @Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse response = notificationPreferenceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification preference created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification preference by ID", description = "Retrieve notification preference by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getById(
            @Parameter(description = "Preference ID") @PathVariable UUID id) {
        NotificationPreferenceResponse response = notificationPreferenceService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notification preference by user", description = "Retrieve notification preference for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        NotificationPreferenceResponse response = notificationPreferenceService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update notification preference", description = "Update notification preference")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> update(
            @Parameter(description = "Preference ID") @PathVariable UUID id,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse response = notificationPreferenceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Notification preference updated successfully", response));
    }

    @PutMapping("/user/{userId}")
    @Operation(summary = "Update notification preference by user", description = "Update notification preference for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updateByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse response = notificationPreferenceService.updateByUserId(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification preference updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification preference", description = "Delete notification preference")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Preference ID") @PathVariable UUID id) {
        notificationPreferenceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Notification preference deleted successfully", null));
    }

    @GetMapping("/user/{userId}/get-or-create")
    @Operation(summary = "Get or create notification preference", description = "Get existing or create default notification preference for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getOrCreateForUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        NotificationPreferenceResponse response = notificationPreferenceService.getOrCreateForUser(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
