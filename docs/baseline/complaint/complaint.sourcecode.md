# Complaint (Khiếu nại) Feature — Source Code Map

## Package Structure

```
com.mentorx.api.feature.moderation
├── package-info.java
├── controller/
│   ├── DisputeController.java
│   ├── ReportController.java
│   └── DisputeEvidenceController.java
├── dto/
│   ├── request/
│   │   ├── DisputeCreateRequest.java
│   │   ├── DisputeRespondRequest.java
│   │   ├── DisputeResolveRequest.java
│   │   ├── ReportCreateRequest.java
│   │   ├── ReportResolveRequest.java
│   │   └── DisputeEvidenceCreateRequest.java
│   └── response/
│       ├── DisputeResponse.java
│       ├── DisputeEvidenceResponse.java
│       └── ReportResponse.java
├── entity/
│   ├── Dispute.java
│   ├── DisputeEvidence.java
│   ├── Report.java
│   └── AdminActivityLog.java
├── enums/
│   ├── DisputeStatus.java
│   ├── DisputeOutcome.java
│   ├── ReportStatus.java
│   ├── ReportTargetType.java
│   └── EvidenceType.java
├── repository/
│   ├── DisputeRepository.java
│   ├── DisputeEvidenceRepository.java
│   ├── ReportRepository.java
│   └── AdminActivityLogRepository.java
├── security/
│   └── DisputePermissionEvaluator.java
└── service/
    ├── DisputeService.java
    ├── DisputeEvidenceService.java
    ├── ReportService.java
    └── impl/
        ├── DisputeServiceImpl.java
        ├── DisputeEvidenceServiceImpl.java
        └── ReportServiceImpl.java
```

## Entities (Target Schema — UUID-Decoupled, No Arbitration/Escrow)

### Dispute

Table `disputes`. All person references are UUID columns — no `@ManyToOne User` joins.

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| complainantId | UUID | NOT NULL, INDEXED |
| respondentId | UUID | NOT NULL, INDEXED |
| sessionId | UUID | nullable — link to mentoring session |
| bookingId | UUID | nullable — link to booking; sole financial reference |
| title | String | max 200 |
| description | String | @Column(columnDefinition = "TEXT") |
| complaintCategory | String | SESSION_QUALITY, CONDUCT, SCHEDULING, MISREPRESENTATION, POLICY_VIOLATION, OTHER |
| status | DisputeStatus | managed via `transitionStatus()` |
| priorityLevel | Integer | 1-5, default 3 |
| mediatorId | UUID | nullable — assigned platform moderator |
| mediatorAssignedAt | LocalDateTime | |
| respondentNotifiedAt | LocalDateTime | |
| respondentRespondedAt | LocalDateTime | |
| respondentResponse | String | @Column(columnDefinition = "TEXT") |
| responseDeadline | LocalDateTime | default: created + 3 days |
| mediationStartedAt | LocalDateTime | |
| resolvedAt | LocalDateTime | |
| outcome | DisputeOutcome | nullable until resolved |
| resolutionDetails | String | |
| resolutionTimeHours | Double | computed on resolve |
| slaMet | Boolean | computed on resolve |
| slaDeadline | LocalDateTime | set by @PrePersist based on priority |
| createdAt, updatedAt | LocalDateTime | inherited from BaseEntity |

**Entity lifecycle methods:**

```java
public void transitionStatus(DisputeStatus newStatus) {
    if (!this.status.canTransitionTo(newStatus)) {
        throw new BusinessException(ErrorCode.INVALID_DISPUTE_STATUS,
            "Cannot transition from " + this.status + " to " + newStatus);
    }
    this.status = newStatus;
}

public void assignMediator(UUID mediatorId) {
    transitionStatus(DisputeStatus.IN_MEDIATION);
    this.mediatorId = mediatorId;
    this.mediatorAssignedAt = LocalDateTime.now();
    this.mediationStartedAt = LocalDateTime.now();
}

public void resolve(DisputeOutcome outcome, String details) {
    transitionStatus(DisputeStatus.RESOLVED);
    this.outcome = outcome;
    this.resolutionDetails = details;
    this.resolvedAt = LocalDateTime.now();
    this.resolutionTimeHours = (double) Duration.between(getCreatedAt(), this.resolvedAt).toHours();
    this.slaMet = this.resolvedAt.isBefore(this.slaDeadline);
}

public void withdraw() {
    transitionStatus(DisputeStatus.WITHDRAWN);
}

public void expire() {
    transitionStatus(DisputeStatus.EXPIRED);
}
```

### DisputeEvidence

Table `dispute_evidence`. Fully implemented with review and flagging workflow.

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| disputeId | UUID | NOT NULL, INDEXED — FK to disputes |
| submittedByUserId | UUID | NOT NULL |
| evidenceType | EvidenceType | enum |
| title | String | max 200 |
| description | String | max 1000 |
| fileUrl | String | S3/CDN URL |
| filename | String | original upload name |
| mimeType | String | |
| fileSize | Long | bytes |
| isReviewed | Boolean | default false |
| reviewedAt | LocalDateTime | nullable |
| reviewedByUserId | UUID | nullable |
| reviewNotes | String | nullable |
| isFlagged | Boolean | default false |
| flagReason | String | nullable |
| createdAt, updatedAt | LocalDateTime | |

### Report

Table `reports`. Content moderation. UUID-decoupled.

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| reporterId | UUID | NOT NULL |
| targetType | ReportTargetType | enum |
| targetId | UUID | |
| reportedUserId | UUID | nullable |
| reportCategory | String | SPAM, HARASSMENT, INAPPROPRIATE_CONTENT, FRAUD, MISINFORMATION, OTHER |
| reason | String | @Column(columnDefinition = "TEXT") |
| reportContext | String | |
| status | ReportStatus | managed via validated transition |
| priorityLevel | Integer | |
| assignedToAdminId | UUID | nullable |
| resolvedAt | LocalDateTime | nullable |
| actionTaken | String | nullable |
| moderatorNotes | String | nullable |
| isUpheld | Boolean | nullable |
| escalationLevel | Integer | |
| evidenceUrls | JSONB | content-moderation URLs (distinct from DisputeEvidence) |
| createdAt, updatedAt | LocalDateTime | |

### AdminActivityLog

Table `admin_activity_logs`. Immutable audit trail — written on every admin-triggered state change.

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | PK |
| adminId | UUID | NOT NULL — moderator/admin who performed the action |
| actionType | String | DISPUTE_MEDIATOR_ASSIGNED, DISPUTE_RESOLVED, REPORT_ASSIGNED, REPORT_RESOLVED, EVIDENCE_REVIEWED, EVIDENCE_FLAGGED |
| actionCategory | String | MODERATION, USER_MANAGEMENT, CONTENT_MANAGEMENT |
| description | String | human-readable summary |
| targetType | String | DISPUTE, REPORT, EVIDENCE |
| targetId | UUID | |
| affectedUserId | UUID | nullable |
| severityLevel | String | INFO, WARNING, CRITICAL |
| previousState | JSONB | state before action |
| newState | JSONB | state after action |
| metadata | JSONB | additional context |
| adminIp, adminUserAgent | String | |
| createdAt | LocalDateTime | |

---

## Enums

### DisputeStatus (9 values) — with `allowedTransitions`

```java
public enum DisputeStatus {
    OPEN(Set.of(AWAITING_RESPONSE, WITHDRAWN, EXPIRED)),
    AWAITING_RESPONSE(Set.of(INVESTIGATING, WITHDRAWN, EXPIRED)),
    INVESTIGATING(Set.of(EVIDENCE_REVIEW, IN_MEDIATION, WITHDRAWN, EXPIRED)),
    EVIDENCE_REVIEW(Set.of(IN_MEDIATION, WITHDRAWN, EXPIRED)),
    IN_MEDIATION(Set.of(RESOLVED, WITHDRAWN, EXPIRED)),
    RESOLVED(Set.of(CLOSED)),
    CLOSED(Collections.emptySet()),
    WITHDRAWN(Collections.emptySet()),
    EXPIRED(Collections.emptySet());

    private final Set<DisputeStatus> allowedTransitions;

    DisputeStatus(Set<DisputeStatus> allowed) {
        this.allowedTransitions = allowed;
    }

    public boolean canTransitionTo(DisputeStatus target) {
        return this.allowedTransitions.contains(target);
    }
}
```

### DisputeOutcome (7 values)

`FAVOR_COMPLAINANT`, `FAVOR_RESPONDENT`, `COMPROMISE`, `MUTUAL_AGREEMENT`, `INVALID_COMPLAINT`, `WARNING_ISSUED`, `NO_OUTCOME`

### ReportStatus (7 values) — with `allowedTransitions`

```java
PENDING      → UNDER_REVIEW, DISMISSED
UNDER_REVIEW → ESCALATED, RESOLVED, DISMISSED, ON_HOLD
ESCALATED    → UNDER_REVIEW, RESOLVED, DISMISSED
ON_HOLD      → UNDER_REVIEW, DISMISSED
RESOLVED     → CLOSED
DISMISSED    → CLOSED
CLOSED       → (terminal)
```

### ReportTargetType (9 values)

`USER_PROFILE`, `MENTOR_PROFILE`, `SESSION`, `REVIEW`, `MESSAGE`, `COMMENT`, `COURSE`, `COURSE_CONTENT`, `PLATFORM_ISSUE`

### EvidenceType (7 values)

`SCREENSHOT`, `DOCUMENT`, `VIDEO`, `AUDIO`, `CHAT_LOG`, `EMAIL`, `OTHER`

---

## Controllers — with `@PreAuthorize` Guards

### DisputeController

| Method | Endpoint | @PreAuthorize |
|--------|----------|--------------|
| POST | `/api/disputes` | `isAuthenticated()` |
| GET | `/api/disputes/{id}` | `@disputeEvaluator.isParty(#id, authentication.principal)` |
| GET | `/api/disputes/user/{userId}` | `#userId == authentication.principal.id` |
| POST | `/api/disputes/{id}/respond` | `@disputeEvaluator.isRespondent(#id, authentication.principal)` |
| POST | `/api/disputes/{id}/assign-mediator` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| POST | `/api/disputes/{id}/resolve` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| POST | `/api/disputes/{id}/withdraw` | `@disputeEvaluator.isComplainant(#id, authentication.principal)` |
| GET | `/api/disputes` | `hasAnyRole('MODERATOR', 'ADMIN')` |

### DisputeEvidenceController

| Method | Endpoint | @PreAuthorize |
|--------|----------|--------------|
| POST | `/api/disputes/{id}/evidence` | `@disputeEvaluator.isParty(#id, authentication.principal)` |
| GET | `/api/disputes/{id}/evidence` | `@disputeEvaluator.isParty(#id, authentication.principal)` |

### ReportController

| Method | Endpoint | @PreAuthorize |
|--------|----------|--------------|
| POST | `/api/reports` | `isAuthenticated()` |
| GET | `/api/reports/{id}` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| GET | `/api/reports` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| POST | `/api/reports/{id}/assign` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| POST | `/api/reports/{id}/resolve` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| POST | `/api/reports/{id}/escalate` | `hasAnyRole('MODERATOR', 'ADMIN')` |

---

## Repositories

### DisputeRepository

```java
@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    Page<Dispute> findByComplainantIdOrRespondentId(UUID complainantId, UUID respondentId, Pageable pageable);
    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);
    List<Dispute> findByStatusAndResponseDeadlineBefore(DisputeStatus status, LocalDateTime cutoff);
}
```

### DisputeEvidenceRepository

```java
@Repository
public interface DisputeEvidenceRepository extends JpaRepository<DisputeEvidence, UUID> {
    List<DisputeEvidence> findByDisputeId(UUID disputeId);
    List<DisputeEvidence> findByDisputeIdAndSubmittedByUserId(UUID disputeId, UUID userId);
}
```

### ReportRepository

```java
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
}
```

### AdminActivityLogRepository

```java
@Repository
public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, UUID> {
    List<AdminActivityLog> findByTargetTypeAndTargetId(String targetType, UUID targetId, Sort sort);
    Page<AdminActivityLog> findByAdminId(UUID adminId, Pageable pageable);
}
```

---

## Security

### DisputePermissionEvaluator

```java
@Component("disputeEvaluator")
public class DisputePermissionEvaluator {

    private final DisputeRepository disputeRepository;

    public DisputePermissionEvaluator(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public boolean isParty(UUID disputeId, CustomUserDetails principal) {
        Dispute d = disputeRepository.findById(disputeId).orElse(null);
        return d != null
            && (d.getComplainantId().equals(principal.getId())
                || d.getRespondentId().equals(principal.getId()));
    }

    public boolean isComplainant(UUID disputeId, CustomUserDetails principal) {
        Dispute d = disputeRepository.findById(disputeId).orElse(null);
        return d != null && d.getComplainantId().equals(principal.getId());
    }

    public boolean isRespondent(UUID disputeId, CustomUserDetails principal) {
        Dispute d = disputeRepository.findById(disputeId).orElse(null);
        return d != null && d.getRespondentId().equals(principal.getId());
    }
}
```

---

## Error Codes

```java
DISPUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "Dispute not found")
DISPUTE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Duplicate dispute for this booking")
INVALID_DISPUTE_STATUS(HttpStatus.BAD_REQUEST, "Invalid dispute status transition")
REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "Report not found")
EVIDENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Evidence not found")
NOT_DISPUTE_PARTY(HttpStatus.FORBIDDEN, "You are not a party to this dispute")
DISPUTE_INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid dispute input")
```

---

## Cross-Service Dependencies

| Service | Integration Point |
|---------|------------------|
| User Profile Service | Resolve UUID → name/avatar at gateway layer (not via JPA) |
| Notification Service | Push/email on complaint status changes |
| Bookings/Sessions Service | `sessionId`/`bookingId` references for context |
| Payments Service | Financial disputes resolved independently; this module stores `bookingId` for correlation only |
