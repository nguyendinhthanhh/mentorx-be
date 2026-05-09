package com.mentorx.api.feature.mentor.service;

import com.mentorx.api.feature.mentor.dto.request.MentorProfileAssetRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorProfileAssetResponse;
import com.mentorx.api.feature.mentor.enums.MentorProfileAssetType;

import java.util.List;
import java.util.UUID;

public interface MentorProfileAssetService {

    List<MentorProfileAssetResponse> getAssets(UUID userId, MentorProfileAssetType type);

    MentorProfileAssetResponse createAsset(UUID userId, MentorProfileAssetRequest request);

    MentorProfileAssetResponse updateAsset(UUID assetId, MentorProfileAssetRequest request);

    void deleteAsset(UUID assetId);
}
