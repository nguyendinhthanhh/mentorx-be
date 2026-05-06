package com.mentorx.api.feature.user.service;

import java.util.List;
import java.util.Map;

public interface OnboardingService {
    Object completeRole(String role);
    Object completeInterests(List<Integer> categoryIds);
    Object completeSkills(List<Map<String, Object>> skills);
    Object completePreferences(Map<String, Object> preferences);
    Object completeGoals(List<String> goals);
    Object completeProfile(Map<String, Object> profileData);
}
