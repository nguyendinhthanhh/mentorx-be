package com.mentorx.api.feature.system.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.system.dto.request.PlatformSettingRequest;
import com.mentorx.api.feature.system.dto.response.PlatformSettingResponse;
import com.mentorx.api.feature.system.service.PlatformSettingService;
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
@RequestMapping("/api/system/platform-settings")
@RequiredArgsConstructor
@Tag(name = "Platform Settings", description = "APIs for managing platform settings")
public class PlatformSettingController {

    private final PlatformSettingService platformSettingService;

    @PostMapping
    @Operation(summary = "Create platform setting", description = "Create a new platform setting (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlatformSettingResponse>> create(
            @Valid @RequestBody PlatformSettingRequest request) {
        PlatformSettingResponse response = platformSettingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Platform setting created successfully", response));
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get platform setting by key", description = "Retrieve platform setting by key (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlatformSettingResponse>> getByKey(
            @Parameter(description = "Setting key") @PathVariable String key) {
        PlatformSettingResponse response = platformSettingService.getByKey(key);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update platform setting", description = "Update an existing platform setting (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PlatformSettingResponse>> update(
            @Parameter(description = "Setting key") @PathVariable String key,
            @Valid @RequestBody PlatformSettingRequest request) {
        PlatformSettingResponse response = platformSettingService.update(key, request);
        return ResponseEntity.ok(ApiResponse.success("Platform setting updated successfully", response));
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "Delete platform setting", description = "Delete a platform setting (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Setting key") @PathVariable String key) {
        platformSettingService.delete(key);
        return ResponseEntity.ok(ApiResponse.success("Platform setting deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all platform settings", description = "Retrieve paginated list of all platform settings (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PlatformSettingResponse>>> getAll(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "key") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PlatformSettingResponse> response = platformSettingService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all platform settings (list)", description = "Retrieve all platform settings as list (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PlatformSettingResponse>>> getAllList() {
        List<PlatformSettingResponse> response = platformSettingService.getAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{key}/value")
    @Operation(summary = "Get setting value", description = "Get value of a platform setting (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getValue(
            @Parameter(description = "Setting key") @PathVariable String key) {
        String value = platformSettingService.getValue(key);
        return ResponseEntity.ok(ApiResponse.success(value));
    }

    @PatchMapping("/{key}/value")
    @Operation(summary = "Update setting value", description = "Update value of a platform setting (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateValue(
            @Parameter(description = "Setting key") @PathVariable String key,
            @Parameter(description = "New value") @RequestParam String value,
            @Parameter(description = "Updated by user ID") @RequestParam UUID updatedBy) {
        platformSettingService.updateValue(key, value, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Setting value updated successfully", null));
    }
}
