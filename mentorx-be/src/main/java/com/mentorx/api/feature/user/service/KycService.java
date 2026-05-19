package com.mentorx.api.feature.user.service;

import com.mentorx.api.feature.user.dto.KycStatusResponse;
import com.mentorx.api.feature.user.dto.KycSubmitRequest;

import java.util.UUID;

public interface KycService {
    KycStatusResponse submitKyc(UUID userId, KycSubmitRequest request);
    void adminReviewKyc(UUID mentorProfileId, boolean approved, String rejectionReason, UUID adminId);
    KycStatusResponse getKycStatus(UUID userId);
}
