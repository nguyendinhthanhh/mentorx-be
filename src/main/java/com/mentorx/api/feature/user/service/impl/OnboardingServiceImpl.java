package com.mentorx.api.feature.user.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import com.mentorx.api.feature.user.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public Object completeRole(String role) {
        User user = getCurrentUser();
        log.info("Completing role step for user {}: {}", user.getEmail(), role);
        user.setIsMentor("MENTOR".equalsIgnoreCase(role) || "BOTH".equalsIgnoreCase(role));
        userRepository.save(user);
        return Map.of("step", 1, "nextStep", 2);
    }

    @Override
    @Transactional
    public Object completeInterests(List<Integer> categoryIds) {
        log.info("Completing interests step for user {}", getCurrentUser().getEmail());
        // Logic to save user interests
        return Map.of("step", 2, "nextStep", 3);
    }

    @Override
    @Transactional
    public Object completeSkills(List<Map<String, Object>> skills) {
        log.info("Completing skills step for user {}", getCurrentUser().getEmail());
        // Logic to save user skills
        return Map.of("step", 3, "nextStep", 4);
    }

    @Override
    @Transactional
    public Object completePreferences(Map<String, Object> preferences) {
        log.info("Completing preferences step for user {}", getCurrentUser().getEmail());
        // Logic to save user preferences
        return Map.of("step", 4, "nextStep", 5);
    }

    @Override
    @Transactional
    public Object completeGoals(List<String> goals) {
        log.info("Completing goals step for user {}", getCurrentUser().getEmail());
        // Logic to save user goals
        return Map.of("step", 5, "nextStep", 6);
    }

    @Override
    @Transactional
    public Object completeProfile(Map<String, Object> profileData) {
        User user = getCurrentUser();
        log.info("Completing profile step for user {}", user.getEmail());
        
        if (profileData.containsKey("displayName")) {
            user.setDisplayName((String) profileData.get("displayName"));
        }
        if (profileData.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) profileData.get("avatarUrl"));
        }
        
        user.setStatus(com.mentorx.api.common.enums.UserStatus.ACTIVE);
        userRepository.save(user);
        
        return Map.of("step", 6, "completed", true);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
