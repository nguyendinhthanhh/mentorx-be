package com.mentorx.api.feature.analytics.service.impl;

import com.mentorx.api.feature.analytics.dto.request.ViewEventRequest;
import com.mentorx.api.feature.analytics.dto.response.EarningsSnapshotResponse;
import com.mentorx.api.feature.analytics.entity.EarningsDailySnapshot;
import com.mentorx.api.feature.analytics.entity.ViewEvent;
import com.mentorx.api.feature.analytics.repository.EarningsDailySnapshotRepository;
import com.mentorx.api.feature.analytics.repository.ViewEventRepository;
import com.mentorx.api.feature.analytics.service.AnalyticsService;
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
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ViewEventRepository viewEventRepository;
    private final EarningsDailySnapshotRepository earningsDailySnapshotRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordView(ViewEventRequest request, UUID viewerId) {
        User viewer = null;
        if (viewerId != null) {
            viewer = userRepository.findById(viewerId).orElse(null);
        }

        ViewEvent event = new ViewEvent();
        event.setTargetType(request.targetType());
        event.setTargetId(request.targetId());
        event.setIpAddress(request.ipAddress());
        event.setViewer(viewer);

        viewEventRepository.save(event);
    }

    @Override
    public long getViewCount(String targetType, UUID targetId) {
        return viewEventRepository.countByTargetTypeAndTargetId(targetType, targetId);
    }

    @Override
    public Page<EarningsSnapshotResponse> getUserEarningsSnapshots(UUID userId, Pageable pageable) {
        return earningsDailySnapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId, pageable)
                .map(this::toResponse);
    }

    private EarningsSnapshotResponse toResponse(EarningsDailySnapshot snapshot) {
        return new EarningsSnapshotResponse(
                snapshot.getId(),
                snapshot.getUser().getId(),
                snapshot.getSnapshotDate(),
                snapshot.getEarnedMxc(),
                snapshot.getWithdrawnMxc(),
                snapshot.getPlatformFeeMxc(),
                snapshot.getJobsCompleted(),
                snapshot.getCoursesSold(),
                snapshot.getEscrowBalanceMxc(),
                snapshot.getAvailableBalanceMxc(),
                snapshot.getEarnedFromMentoringMxc(),
                snapshot.getEarnedFromFreelanceMxc(),
                snapshot.getEarnedFromCoursesMxc(),
                snapshot.getProposalsSent(),
                snapshot.getProposalsAccepted(),
                snapshot.getContractsActive(),
                snapshot.getContractsCompleted(),
                snapshot.getCourseEnrollments()
        );
    }
}
