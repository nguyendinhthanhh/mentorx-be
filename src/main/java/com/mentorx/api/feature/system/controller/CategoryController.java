package com.mentorx.api.feature.system.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.system.dto.request.CategoryRequest;
import com.mentorx.api.feature.system.dto.response.CategoryResponse;
import com.mentorx.api.feature.system.service.CategoryService;
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
@RequestMapping("/api/system/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "APIs for managing categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @Parameter(description = "Category ID") @PathVariable Integer id) {
        CategoryResponse response = categoryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Retrieve category by slug")
    public ResponseEntity<ApiResponse<CategoryResponse>> getBySlug(
            @Parameter(description = "Category slug") @PathVariable String slug) {
        CategoryResponse response = categoryService.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @Parameter(description = "Category ID") @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Category ID") @PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve paginated list of all categories")
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAll(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by") @RequestParam(defaultValue = "displayOrder") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CategoryResponse> response = categoryService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active categories", description = "Retrieve all active categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActive() {
        List<CategoryResponse> response = categoryService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "Get categories by parent", description = "Retrieve categories by parent ID")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getByParentId(
            @Parameter(description = "Parent category ID") @PathVariable Integer parentId) {
        List<CategoryResponse> response = categoryService.getByParentId(parentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Retrieve all root categories (no parent)")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle category active status", description = "Toggle active/inactive status (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleActive(
            @Parameter(description = "Category ID") @PathVariable Integer id) {
        categoryService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success("Category status toggled successfully", null));
    }

    @PatchMapping("/{id}/display-order")
    @Operation(summary = "Update display order", description = "Update category display order (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateDisplayOrder(
            @Parameter(description = "Category ID") @PathVariable Integer id,
            @Parameter(description = "Display order") @RequestParam Short displayOrder) {
        categoryService.updateDisplayOrder(id, displayOrder);
        return ResponseEntity.ok(ApiResponse.success("Display order updated successfully", null));
    }
}
