package com.mentorx.api.feature.job.service.impl;

import com.mentorx.api.feature.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCronService {

    private final JobRepository jobRepository;

    /**
     * Run every hour to expire jobs that have passed their deadline
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expirePastDeadlineJobs() {
        log.info("Starting job expiration cron task...");
        try {
            int expiredCount = jobRepository.expirePastDeadlineJobs();
            log.info("Successfully expired {} past-deadline jobs", expiredCount);
        } catch (Exception e) {
            log.error("Error running job expiration cron task", e);
        }
    }
}
