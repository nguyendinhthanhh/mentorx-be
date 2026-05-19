package com.mentorx.api.feature.matching.service;

import com.mentorx.api.feature.matching.dto.request.UserInterestProfileRequest;
import com.mentorx.api.feature.matching.dto.response.UserInterestProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserInterestProfileService {
    
    UserInterestProfileResponse create(UserInterestProfileRequest request);
    
    UserInterestProfileResponse getById(UUID id);
    
    UserInterestProfileResponse update(UUID id, UserInterestProfileRequest request);
    
    void delete(UUID id);
    
    Page<UserInterestProfileResponse> getAll(Pageable pageable);
    
    List<UserInterestProfileResponse> getByUserId(UUID userId);
    
    List<UserInterestProfileResponse> getTopInterestsForUser(UUID userId, int limit);
    
    void recordInteraction(UUID userId, Integer categoryId, int timeSpentMinutes);
    
    void applyDecay(UUID userId);
}
