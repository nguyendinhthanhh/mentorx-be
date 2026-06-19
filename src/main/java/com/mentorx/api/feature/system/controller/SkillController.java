package com.mentorx.api.feature.system.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.system.dto.request.SkillRequest;
import com.mentorx.api.feature.system.dto.response.SkillResponse;
import com.mentorx.api.feature.system.service.SkillService;
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

@RestController
@RequestMapping("/api/system/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "APIs for managing skills")
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    @Operation(summary = "Create skill", description = "Create a new skill")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SkillResponse>> create(
            @Valid @RequestBody SkillRequest request) {
        SkillResponse response = skillService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Skill created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by ID", description = "Retrieve skill by ID")
    public ResponseEntity<ApiResponse<SkillResponse>> getById(
            @Parameter(description = "Skill ID") @PathVariable Integer id) {
        SkillResponse response = skillService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get skill by slug", description = "Retrieve skill by slug")
    public ResponseEntity<ApiResponse<SkillResponse>> getBySlug(
            @Parameter(description = "Skill slug") @PathVariable String slug) {
        SkillResponse response = skillService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update skill", description = "Update an existing skill (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillResponse>> update(
            @Parameter(description = "Skill ID") @PathVariable Integer id,
            @Valid @RequestBody SkillRequest request) {
        SkillResponse response = skillService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Skill updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete skill", description = "Delete a skill (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Skill ID") @PathVariable Integer id) {
        skillService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Skill deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all skills", description = "Retrieve paginated list of all skills")
    public ResponseEntity<ApiResponse<Page<SkillResponse>>> getAll(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "labelEn") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<SkillResponse> response = skillService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active skills", description = "Retrieve all active skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getAllActive() {
        List<SkillResponse> response = skillService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search skills", description = "Search skills by name")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> searchByName(
            @Parameter(description = "Search query") @RequestParam String query) {
        List<SkillResponse> response = skillService.searchByName(query);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle skill active status", description = "Toggle active/inactive status (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleActive(
            @Parameter(description = "Skill ID") @PathVariable Integer id) {
        skillService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Skill status toggled successfully", null));
    }

    @GetMapping("/count")
    @Operation(summary = "Count skills", description = "Get total number of skills")
    public ResponseEntity<ApiResponse<Long>> count() {
        long count = skillService.count();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
