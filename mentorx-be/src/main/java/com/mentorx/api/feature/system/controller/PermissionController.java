package com.mentorx.api.feature.system.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.system.dto.request.PermissionRequest;
import com.mentorx.api.feature.system.dto.response.PermissionResponse;
import com.mentorx.api.feature.system.service.PermissionService;
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
@RequestMapping("/api/system/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "APIs for managing permissions")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Create permission", description = "Create a new permission (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> create(
            @Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID", description = "Retrieve permission by ID (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getById(
            @Parameter(description = "Permission ID") @PathVariable UUID id) {
        PermissionResponse response = permissionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/key/{permissionKey}")
    @Operation(summary = "Get permission by key", description = "Retrieve permission by key (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getByKey(
            @Parameter(description = "Permission key") @PathVariable String permissionKey) {
        PermissionResponse response = permissionService.getByKey(permissionKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update permission", description = "Update an existing permission (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(
            @Parameter(description = "Permission ID") @PathVariable UUID id,
            @Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission", description = "Delete a permission (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Permission ID") @PathVariable UUID id) {
        permissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Permission deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all permissions", description = "Retrieve paginated list of all permissions (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> getAll(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "permissionKey") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PermissionResponse> response = permissionService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all permissions (list)", description = "Retrieve all permissions as list (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllList() {
        List<PermissionResponse> response = permissionService.getAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "Get permissions by role", description = "Retrieve all permissions for a role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getByRoleId(
            @Parameter(description = "Role ID") @PathVariable UUID roleId) {
        List<PermissionResponse> response = permissionService.getByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/role/{roleId}/assign/{permissionId}")
    @Operation(summary = "Assign permission to role", description = "Assign a permission to a role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignToRole(
            @Parameter(description = "Role ID") @PathVariable UUID roleId,
            @Parameter(description = "Permission ID") @PathVariable UUID permissionId) {
        permissionService.assignToRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission assigned to role successfully", null));
    }

    @DeleteMapping("/role/{roleId}/remove/{permissionId}")
    @Operation(summary = "Remove permission from role", description = "Remove a permission from a role (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeFromRole(
            @Parameter(description = "Role ID") @PathVariable UUID roleId,
            @Parameter(description = "Permission ID") @PathVariable UUID permissionId) {
        permissionService.removeFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed from role successfully", null));
    }
}
