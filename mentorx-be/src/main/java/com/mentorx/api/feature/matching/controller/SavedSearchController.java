package com.mentorx.api.feature.matching.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.matching.dto.request.SavedSearchRequest;
import com.mentorx.api.feature.matching.dto.response.SavedSearchResponse;
import com.mentorx.api.feature.matching.service.SavedSearchService;
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
@RequestMapping("/api/matching/saved-searches")
@RequiredArgsConstructor
@Tag(name = "Saved Searches", description = "APIs for managing saved searches")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    @PostMapping
    @Operation(summary = "Create saved search", description = "Create a new saved search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> create(
            @Valid @RequestBody SavedSearchRequest request) {
        SavedSearchResponse response = savedSearchService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Saved search created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get saved search by ID", description = "Retrieve saved search by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> getById(
            @Parameter(description = "Saved search ID") @PathVariable UUID id) {
        SavedSearchResponse response = savedSearchService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update saved search", description = "Update an existing saved search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> update(
            @Parameter(description = "Saved search ID") @PathVariable UUID id,
            @Valid @RequestBody SavedSearchRequest request) {
        SavedSearchResponse response = savedSearchService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Saved search updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete saved search", description = "Delete a saved search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Saved search ID") @PathVariable UUID id) {
        savedSearchService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Saved search deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all saved searches", description = "Retrieve paginated list of all saved searches")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Page<SavedSearchResponse>>> getAll(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<SavedSearchResponse> response = savedSearchService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get saved searches by user", description = "Retrieve all saved searches for a specific user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SavedSearchResponse>>> getByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        List<SavedSearchResponse> response = savedSearchService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/paginated")
    @Operation(summary = "Get saved searches by user (paginated)", description = "Retrieve paginated saved searches for a specific user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<SavedSearchResponse>>> getByUserIdPaginated(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SavedSearchResponse> response = savedSearchService.getByUserIdPaginated(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Count saved searches by user", description = "Get the count of saved searches for a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> countByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        long count = savedSearchService.countByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
