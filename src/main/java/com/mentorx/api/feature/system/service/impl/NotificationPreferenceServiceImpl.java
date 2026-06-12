package com.mentorx.api.feature.system.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.common.security.MentorModeAccessService;
import com.mentorx.api.feature.system.dto.request.NotificationPreferenceRequest;
import com.mentorx.api.feature.system.dto.response.NotificationPreferenceResponse;
import com.mentorx.api.feature.system.entity.NotificationPreference;
import com.mentorx.api.feature.system.mapper.SystemMapper;
import com.mentorx.api.feature.system.repository.NotificationPreferenceRepository;
import com.mentorx.api.feature.system.service.NotificationPreferenceService;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;
    private final SystemMapper systemMapper;
    private final MentorModeAccessService mentorModeAccessService;

    @Override
    @Transactional
    public NotificationPreferenceResponse create(NotificationPreferenceRequest request) {
        log.info("Creating notification preference for user: {}", request.userId());
        mentorModeAccessService.requireSelfOrAdmin(request.userId());

        // Verify user exists
        if (!userRepository.existsById(request.userId())) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // Check if preference already exists
        if (notificationPreferenceRepository.existsByUserId(request.userId())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Notification preference already exists for this user");
        }

        NotificationPreference entity = systemMapper.toNotificationPreference(request);
        entity.setUserId(request.userId());
        entity.setUpdatedAt(LocalDateTime.now());

        // Set defaults
        if (entity.getEmailEnabled() == null) entity.setEmailEnabled(true);
        if (entity.getPushEnabled() == null) entity.setPushEnabled(true);
        if (entity.getInAppEnabled() == null) entity.setInAppEnabled(true);
        if (entity.getEmailTypeSettings() == null) entity.setEmailTypeSettings("{}");
        if (entity.getPushTypeSettings() == null) entity.setPushTypeSettings("{}");

        NotificationPreference saved = notificationPreferenceRepository.save(entity);
        log.info("Created notification preference for user: {}", saved.getUserId());

        return systemMapper.toNotificationPreferenceResponse(saved);
    }

    @Override
    public NotificationPreferenceResponse getById(UUID id) {
        log.debug("Fetching notification preference with ID: {}", id);
        NotificationPreference entity = notificationPreferenceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        mentorModeAccessService.requireSelfOrAdmin(entity.getUserId());
        return systemMapper.toNotificationPreferenceResponse(entity);
    }

    @Override
    public NotificationPreferenceResponse getByUserId(UUID userId) {
        log.debug("Fetching notification preference for user: {}", userId);
        mentorModeAccessService.requireSelfOrAdmin(userId);
        NotificationPreference entity = notificationPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return systemMapper.toNotificationPreferenceResponse(entity);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse update(UUID id, NotificationPreferenceRequest request) {
        log.info("Updating notification preference with ID: {}", id);

        NotificationPreference entity = notificationPreferenceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        mentorModeAccessService.requireSelfOrAdmin(entity.getUserId());

        systemMapper.updateNotificationPreference(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        NotificationPreference updated = notificationPreferenceRepository.save(entity);
        log.info("Updated notification preference with ID: {}", id);

        return systemMapper.toNotificationPreferenceResponse(updated);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updateByUserId(UUID userId, NotificationPreferenceRequest request) {
        log.info("Updating notification preference for user: {}", userId);
        mentorModeAccessService.requireSelfOrAdmin(userId);

        NotificationPreference entity = notificationPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        systemMapper.updateNotificationPreference(entity, request);
        entity.setUpdatedAt(LocalDateTime.now());

        NotificationPreference updated = notificationPreferenceRepository.save(entity);
        log.info("Updated notification preference for user: {}", userId);

        return systemMapper.toNotificationPreferenceResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting notification preference with ID: {}", id);
        NotificationPreference entity = notificationPreferenceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        mentorModeAccessService.requireSelfOrAdmin(entity.getUserId());
        notificationPreferenceRepository.delete(entity);
        log.info("Deleted notification preference with ID: {}", id);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse getOrCreateForUser(UUID userId) {
        log.debug("Getting or creating notification preference for user: {}", userId);
        mentorModeAccessService.requireSelfOrAdmin(userId);

        return notificationPreferenceRepository.findByUserId(userId)
                .map(systemMapper::toNotificationPreferenceResponse)
                .orElseGet(() -> {
                    log.info("Creating default notification preference for user: {}", userId);
                    NotificationPreferenceRequest request = NotificationPreferenceRequest.builder()
                            .userId(userId)
                            .emailEnabled(true)
                            .pushEnabled(true)
                            .inAppEnabled(true)
                            .emailTypeSettings("{}")
                            .pushTypeSettings("{}")
                            .build();
                    return create(request);
                });
    }
}
