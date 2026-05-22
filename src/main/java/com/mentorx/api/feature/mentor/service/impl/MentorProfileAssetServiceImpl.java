package com.mentorx.api.feature.mentor.service.impl;

import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.mentor.dto.request.MentorProfileAssetRequest;
import com.mentorx.api.feature.mentor.dto.response.MentorProfileAssetResponse;
import com.mentorx.api.feature.mentor.entity.MentorProfileAsset;
import com.mentorx.api.feature.mentor.enums.MentorProfileAssetType;
import com.mentorx.api.feature.mentor.repository.MentorProfileAssetRepository;
import com.mentorx.api.feature.mentor.service.MentorProfileAssetService;
import com.mentorx.api.feature.user.entity.MentorProfile;
import com.mentorx.api.feature.user.repository.MentorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorProfileAssetServiceImpl implements MentorProfileAssetService {

    private final MentorProfileAssetRepository assetRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorModeAccessService mentorModeAccessService;

    @Override
    @Transactional(readOnly = true)
    public List<MentorProfileAssetResponse> getAssets(UUID userId, MentorProfileAssetType type) {
        MentorProfile profile = findMentorProfileByUserId(userId);
        List<MentorProfileAsset> assets = type == null
                ? assetRepository.findByMentorProfileIdOrderByDisplayOrderAscCreatedAtDesc(profile.getId())
                : assetRepository.findByMentorProfileIdAndTypeOrderByDisplayOrderAscCreatedAtDesc(profile.getId(), type);
        return assets.stream().map(MentorProfileAssetResponse::fromEntity).toList();
    }

    @Override
    @Transactional
    public MentorProfileAssetResponse createAsset(UUID userId, MentorProfileAssetRequest request) {
        mentorModeAccessService.requireApprovedMentorContentAccess(userId);
        MentorProfile profile = findMentorProfileByUserId(userId);
        MentorProfileAsset asset = new MentorProfileAsset();
        asset.setMentorProfileId(profile.getId());
        applyRequest(asset, request);
        return MentorProfileAssetResponse.fromEntity(assetRepository.save(asset));
    }

    @Override
    @Transactional
    public MentorProfileAssetResponse updateAsset(UUID assetId, MentorProfileAssetRequest request) {
        MentorProfileAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        MentorProfile profile = mentorProfileRepository.findById(asset.getMentorProfileId())
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        mentorModeAccessService.requireApprovedMentorContentAccess(profile.getUser().getId());
        applyRequest(asset, request);
        return MentorProfileAssetResponse.fromEntity(assetRepository.save(asset));
    }

    @Override
    @Transactional
    public void deleteAsset(UUID assetId) {
        MentorProfileAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        MentorProfile profile = mentorProfileRepository.findById(asset.getMentorProfileId())
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
        mentorModeAccessService.requireApprovedMentorContentAccess(profile.getUser().getId());
        assetRepository.delete(asset);
    }

    private MentorProfile findMentorProfileByUserId(UUID userId) {
        return mentorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));
    }

    private void applyRequest(MentorProfileAsset asset, MentorProfileAssetRequest request) {
        asset.setType(request.getType());
        asset.setTitle(request.getTitle());
        asset.setDescription(request.getDescription());
        asset.setIssuer(request.getIssuer());
        asset.setFileUrl(request.getFileUrl());
        asset.setIconUrl(request.getIconUrl());
        asset.setIssuedAt(request.getIssuedAt());
        asset.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        asset.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
    }
}
