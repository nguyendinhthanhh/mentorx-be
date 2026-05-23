package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.request.UserPreferenceRequest;
import com.mentorx.api.feature.user.dto.response.UserPreferenceResponse;

public interface UserPreferenceService {

    UserPreferenceResponse getCurrentPreferences();

    UserPreferenceResponse updateCurrentPreferences(UserPreferenceRequest request);
}

