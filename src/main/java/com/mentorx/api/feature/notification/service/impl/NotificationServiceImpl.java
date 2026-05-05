package com.mentorx.api.feature.notification.service.impl;

import com.mentorx.api.common.exception.AppException;
import com.mentorx.api.common.exception.ErrorCode;
import com.mentorx.api.feature.notification.dto.request.NotificationCreateRequest;
import com.mentorx.api.feature.notification.dto.response.NotificationResponse;
import com.mentorx.api.feature.notification.entity.Notification;
import com.mentorx.api.feature.notification.repository.NotificationRepository;
import com.mentorx.api.feature.notification.service.NotificationService;
import com.mentorx.api.feature.user.entity.User;
import com.mentorx.api.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationResponse sendNotification(NotificationCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User sender = null;
        if (request.senderUserId() != null) {
            sender = userRepository.findById(request.senderUserId()).orElse(null);
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setNotificationType(request.notificationType());
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setReferenceId(request.referenceId());
        notification.setReferenceType(request.referenceType());
        notification.setActionUrl(request.actionUrl());
        notification.setIconUrl(request.iconUrl());
        notification.setPriorityLevel(request.priorityLevel() != null ? request.priorityLevel() : 3);
        notification.setData(request.data());
        notification.setCategory(request.category());
        notification.setGroupId(request.groupId());
        notification.setSenderUser(sender);

        return toResponse(notificationRepository.save(notification));
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(UUID userId, boolean unreadOnly, Pageable pageable) {
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndIsReadFalseAndIsDismissedFalse(userId, pageable)
                    .map(this::toResponse);
        }
        return notificationRepository.findByUserIdAndIsDismissedFalse(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalseAndIsDismissedFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = findNotification(notificationId);
        notification.markAsRead();
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public NotificationResponse dismissNotification(UUID notificationId) {
        Notification notification = findNotification(notificationId);
        notification.dismiss();
        return toResponse(notificationRepository.save(notification));
    }

    private Notification findNotification(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.getActionUrl(),
                notification.getIconUrl(),
                notification.getPriorityLevel(),
                notification.getIsRead(),
                notification.getReadAt(),
                notification.getData(),
                notification.getCategory(),
                notification.getGroupId(),
                notification.getIsDismissible(),
                notification.getRequiresAction(),
                notification.getActionTaken(),
                notification.getActionTakenAt(),
                notification.getExpiresAt(),
                notification.getIsExpired(),
                notification.getSenderUser() != null ? notification.getSenderUser().getId() : null,
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
