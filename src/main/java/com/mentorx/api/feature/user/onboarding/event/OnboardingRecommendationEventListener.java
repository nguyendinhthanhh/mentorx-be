package com.mentorx.api.feature.user.onboarding.event;

import com.mentorx.api.feature.matching.service.MatchingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Decouples onboarding finalization from the matching/recommendation subsystem.
 * When a {@link MatchingService} implementation is registered, extend this listener to invoke it.
 */
@Slf4j
@Component
public class OnboardingRecommendationEventListener {

    @Autowired(required = false)
    private MatchingService matchingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOnboardingCompleted(OnboardingCompletedEvent event) {
        if (matchingService == null) {
            log.debug("MatchingService bean not available; skip recommendation trigger for user {}", event.userId());
            return;
        }
        try {
            log.info("Onboarding completed for user {}; recommendation engine should refresh here", event.userId());
        } catch (Exception e) {
            log.warn("Recommendation trigger failed for user {}", event.userId(), e);
        }
    }
}
