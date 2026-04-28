package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.user.dto.request.UserRoleAssignRequest;
import com.mentorx.api.feature.user.dto.response.RoleResponse;
import com.mentorx.api.feature.user.dto.response.UserRoleResponse;
import com.mentorx.api.feature.user.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
@Tag(name = "User Role Management", description = "APIs for user role assignment and management")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping("/assign")
    @Operation(summary = "Assign role to user", description = "Assign a role to a user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
            @Valid @RequestBody UserRoleAssignRequest request,
            Authentication authentication) {
        UUID grantedBy = UUID.fromString(authentication.getName());
        userRoleService.assignRoleToUser(request.userId(), request.roleId(), grantedBy);
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully", null));
    }

    @DeleteMapping("/remove")
    @Operation(summary = "Remove role from user", description = "Remove a role from a user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
            @Parameter(description = "User ID") @RequestParam UUID userId,
            @Parameter(description = "Role ID") @RequestParam Integer roleId) {
        userRoleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", null));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user roles", description = "Get all roles assigned to a user")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or @userService.getUserById(#userId).id == authentication.name")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> getUserRoles(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<UserRoleResponse> roles = userRoleService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Get all available roles in the system")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = userRoleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/roles/{roleId}")
    @Operation(summary = "Get role by ID", description = "Get role information by ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @Parameter(description = "Role ID") @PathVariable Integer roleId) {
        RoleResponse role = userRoleService.getRoleById(roleId);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @GetMapping("/roles/name/{roleName}")
    @Operation(summary = "Get role by name", description = "Get role information by name")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(
            @Parameter(description = "Role name") @PathVariable String roleName) {
        RoleResponse role = userRoleService.getRoleByName(roleName);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @GetMapping("/check/{userId}/role/{roleName}")
    @Operation(summary = "Check if user has role", description = "Check if a user has a specific role")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or @userService.getUserById(#userId).id == authentication.name")
    public ResponseEntity<ApiResponse<Boolean>> hasRole(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Role name") @PathVariable String roleName) {
        boolean hasRole = userRoleService.hasRole(userId, roleName);
        return ResponseEntity.ok(ApiResponse.success(hasRole));
    }

    @GetMapping("/users/role/{roleName}")
    @Operation(summary = "Get users with role", description = "Get all users with a specific role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UUID>>> getUsersWithRole(
            @Parameter(description = "Role name") @PathVariable String roleName) {
        List<UUID> userIds = userRoleService.getUsersWithRole(roleName);
        return ResponseEntity.ok(ApiResponse.success(userIds));
    }

    @GetMapping("/count/role/{roleId}")
    @Operation(summary = "Count users with role", description = "Count users with a specific role")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Long>> countUsersWithRole(
            @Parameter(description = "Role ID") @PathVariable Integer roleId) {
        long count = userRoleService.countUsersWithRole(roleId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}