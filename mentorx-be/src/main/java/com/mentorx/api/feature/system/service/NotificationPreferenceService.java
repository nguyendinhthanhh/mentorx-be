package com.mentorx.api.feature.system.service;

import com.mentorx.api.feature.system.dto.request.NotificationPreferenceRequest;
import com.mentorx.api.feature.system.dto.response.NotificationPreferenceResponse;

import java.util.UUID;

public interface NotificationPreferenceService {
    
    NotificationPreferenceResponse create(NotificationPreferenceRequest request);
    
    NotificationPreferenceResponse getById(UUID id);
    
    NotificationPreferenceResponse getByUserId(UUID userId);
    
    NotificationPreferenceResponse update(UUID id, NotificationPreferenceRequest request);
    
    NotificationPreferenceResponse updateByUserId(UUID userId, NotificationPreferenceRequest request);
    
    void delete(UUID id);
    
    NotificationPreferenceResponse getOrCreateForUser(UUID userId);
}
