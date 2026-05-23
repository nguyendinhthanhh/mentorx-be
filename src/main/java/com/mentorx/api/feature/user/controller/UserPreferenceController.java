package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.user.dto.request.UserPreferenceRequest;
import com.mentorx.api.feature.user.dto.response.UserPreferenceResponse;
import com.mentorx.api.feature.user.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getCurrentPreferences() {
        return ResponseEntity.ok(ApiResponse.success(userPreferenceService.getCurrentPreferences()));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> updateCurrentPreferences(
            @Valid @RequestBody UserPreferenceRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Preferences updated successfully",
                userPreferenceService.updateCurrentPreferences(request)
        ));
    }
}

