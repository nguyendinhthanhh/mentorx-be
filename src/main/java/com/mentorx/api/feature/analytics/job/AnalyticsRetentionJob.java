package com.mentorx.api.feature.analytics.job;

import com.mentorx.api.feature.analytics.repository.ViewEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Phase 1.7: purge {@code view_events} older than 90 days (DEC-010).
 * Runs at 03:00 to stay clear of the earnings aggregation at 02:30 and the
 * feed precomputation at 02:00.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsRetentionJob {

    private static final int RETENTION_DAYS = 90;

    private final ViewEventRepository viewEventRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeOldViews() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = viewEventRepository.deleteOlderThan(cutoff);
        log.info("[Analytics] view_events retention: deleted {} rows older than {} ({} days)",
                deleted, cutoff, RETENTION_DAYS);
    }
}
