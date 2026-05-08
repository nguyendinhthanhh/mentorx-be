package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.enums.MentorStatus;
import com.mentorx.api.common.enums.UserStatus;
import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.user.dto.request.UserCreateRequest;
import com.mentorx.api.feature.user.dto.request.UserUpdateRequest;
import com.mentorx.api.feature.user.dto.response.UserResponse;
import com.mentorx.api.feature.user.service.UserService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user management and profile operations")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve the authenticated user's profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        UserResponse user = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve user information by email")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @Parameter(description = "User email") @PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user profile information")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse user = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Permanently delete a user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @DeleteMapping("/{userId}/soft")
    @Operation(summary = "Soft delete user", description = "Soft delete a user account")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> softDeleteUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        userService.softDeleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User account deactivated", null));
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Update user account status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "New status") @RequestParam UserStatus status) {
        UserResponse user = userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success("User status updated", user));
    }

    @PatchMapping("/{userId}/mentor-status")
    @Operation(summary = "Update mentor status", description = "Update user mentor status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateMentorStatus(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "New mentor status") @RequestParam MentorStatus mentorStatus) {
        UserResponse user = userService.updateMentorStatus(userId, mentorStatus);
        return ResponseEntity.ok(ApiResponse.success("Mentor status updated", user));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve paginated list of users with optional filters")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @Parameter(description = "User status filter") @RequestParam(required = false) UserStatus status,
            @Parameter(description = "Mentor status filter") @RequestParam(required = false) MentorStatus mentorStatus,
            @Parameter(description = "Search term") @RequestParam(required = false) String search,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserResponse> users = userService.getUsersWithFilters(status, mentorStatus, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Full-text search for users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @Parameter(description = "Search query") @RequestParam String query) {
        List<UserResponse> users = userService.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/{userId}/2fa/enable")
    @Operation(summary = "Enable 2FA", description = "Enable two-factor authentication for user")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> enable2FA(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        userService.enable2FA(userId);
        return ResponseEntity.ok(ApiResponse.success("2FA enabled successfully", null));
    }

    @PostMapping("/{userId}/2fa/disable")
    @Operation(summary = "Disable 2FA", description = "Disable two-factor authentication for user")
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> disable2FA(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        userService.disable2FA(userId);
        return ResponseEntity.ok(ApiResponse.success("2FA disabled successfully", null));
    }

    @PostMapping("/{userId}/last-seen")
    @Operation(summary = "Update last seen", description = "Update user's last seen timestamp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateLastSeenAt(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        userService.updateLastSeenAt(userId);
        return ResponseEntity.ok(ApiResponse.success("Last seen updated", null));
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get inactive users", description = "Get users who haven't been active for specified days")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getInactiveUsers(
            @Parameter(description = "Days of inactivity") @RequestParam(defaultValue = "30") int days) {
        List<UserResponse> users = userService.getInactiveUsers(days);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/statistics/total")
    @Operation(summary = "Get total users count", description = "Get total number of users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Long>> getTotalUsersCount() {
        long count = userService.getTotalUsersCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/statistics/active")
    @Operation(summary = "Get active users count", description = "Get number of active users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Long>> getActiveUsersCount() {
        long count = userService.getActiveUsersCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/statistics/mentors")
    @Operation(summary = "Get mentors count", description = "Get number of approved mentors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Long>> getMentorsCount() {
        long count = userService.getMentorsCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/statistics/pending-mentors")
    @Operation(summary = "Get pending mentor applications count", description = "Get number of pending mentor applications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Long>> getPendingMentorApplicationsCount() {
        long count = userService.getPendingMentorApplicationsCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
