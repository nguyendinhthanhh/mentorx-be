package com.mentorx.api.feature.user.controller;

import com.mentorx.api.common.response.ApiResponse;
import com.mentorx.api.feature.user.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/role")
    public ResponseEntity<ApiResponse<Object>> completeRole(@RequestBody Map<String, String> request) {
        System.out.println("DEBUG: OnboardingController.completeRole called with " + request);
        String role = request.get("roleChoice");
        return ResponseEntity.ok(ApiResponse.success("Role saved", onboardingService.completeRole(role)));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Object>> completeCategories(@RequestBody Map<String, Object> request) {
        List<Integer> categoryIds = (List<Integer>) request.get("categoryIds");
        return ResponseEntity.ok(ApiResponse.success("Categories saved", onboardingService.completeInterests(categoryIds)));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<Object>> completeSkills(@RequestBody Map<String, Object> request) {
        List<Map<String, Object>> skills = (List<Map<String, Object>>) request.get("skills");
        return ResponseEntity.ok(ApiResponse.success("Skills saved", onboardingService.completeSkills(skills)));
    }

    @PostMapping("/preferences")
    public ResponseEntity<ApiResponse<Object>> completePreferences(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(ApiResponse.success("Preferences saved", onboardingService.completePreferences(request)));
    }

    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<Object>> completeGoals(@RequestBody Map<String, Object> request) {
        String learningGoals = (String) request.get("learningGoals");
        // Convert single goal to list for service layer
        List<String> goals = learningGoals != null ? List.of(learningGoals) : List.of();
        return ResponseEntity.ok(ApiResponse.success("Goals saved", onboardingService.completeGoals(goals)));
    }

    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<Object>> completeProfile(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(ApiResponse.success("Profile saved", onboardingService.completeProfile(request)));
    }
}
