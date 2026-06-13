package com.mentorx.api.feature.complaint.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum ComplaintStatus {
    OPEN,
    AWAITING_RESPONSE,
    INVESTIGATING,
    EVIDENCE_REVIEW,
    IN_MEDIATION,
    RESOLVED,
    CLOSED,
    WITHDRAWN,
    EXPIRED;

    private Set<ComplaintStatus> allowedTransitions;

    static {
        OPEN.allowedTransitions = Set.of(AWAITING_RESPONSE, WITHDRAWN, EXPIRED);
        AWAITING_RESPONSE.allowedTransitions = Set.of(INVESTIGATING, WITHDRAWN, EXPIRED);
        INVESTIGATING.allowedTransitions = Set.of(EVIDENCE_REVIEW, IN_MEDIATION, WITHDRAWN, EXPIRED);
        EVIDENCE_REVIEW.allowedTransitions = Set.of(IN_MEDIATION, WITHDRAWN, EXPIRED);
        IN_MEDIATION.allowedTransitions = Set.of(RESOLVED, WITHDRAWN, EXPIRED);
        RESOLVED.allowedTransitions = Set.of(CLOSED);
        CLOSED.allowedTransitions = Collections.emptySet();
        WITHDRAWN.allowedTransitions = Collections.emptySet();
        EXPIRED.allowedTransitions = Collections.emptySet();
    }

    public boolean canTransitionTo(ComplaintStatus target) {
        return this.allowedTransitions.contains(target);
    }
}
