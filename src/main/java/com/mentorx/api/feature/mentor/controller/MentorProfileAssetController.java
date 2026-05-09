package com.mentorx.api.feature.mentor.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.mentor.dto.request.MentorProfileAssetRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorProfileAssetResponse;
import com.mentorx.api.feature.mentor.enums.MentorProfileAssetType;
import com.mentorx.api.feature.mentor.service.MentorProfileAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@Tag(name = "Mentor Profile Assets", description = "APIs for managing mentor public profile assets")
public class MentorProfileAssetController {

    private final MentorProfileAssetService assetService;

    @GetMapping("/{userId}/profile-assets")
    @Operation(summary = "Get mentor profile assets", description = "Get achievements, certificates, or documents for a mentor")
    public ResponseEntity<ApiResponse<List<MentorProfileAssetResponse>>> getAssets(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Asset type") @RequestParam(required = false) MentorProfileAssetType type) {
        return ResponseEntity.ok(ApiResponse.success(assetService.getAssets(userId, type)));
    }

    @PostMapping("/{userId}/profile-assets")
    @Operation(summary = "Create mentor profile asset", description = "Create a mentor achievement, certificate, or document")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorProfileAssetResponse>> createAsset(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody MentorProfileAssetRequest request) {
        MentorProfileAssetResponse response = assetService.createAsset(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile asset created successfully", response));
    }

    @PutMapping("/profile-assets/{assetId}")
    @Operation(summary = "Update mentor profile asset", description = "Update a mentor achievement, certificate, or document")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MentorProfileAssetResponse>> updateAsset(
            @Parameter(description = "Asset ID") @PathVariable UUID assetId,
            @Valid @RequestBody MentorProfileAssetRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile asset updated successfully", assetService.updateAsset(assetId, request)));
    }

    @DeleteMapping("/profile-assets/{assetId}")
    @Operation(summary = "Delete mentor profile asset", description = "Delete a mentor achievement, certificate, or document")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @Parameter(description = "Asset ID") @PathVariable UUID assetId) {
        assetService.deleteAsset(assetId);
        return ResponseEntity.ok(ApiResponse.success("Profile asset deleted successfully", null));
    }
}
