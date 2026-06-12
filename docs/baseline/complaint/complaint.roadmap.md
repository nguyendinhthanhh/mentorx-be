# Complaint (Khiếu nại) Feature — Roadmap

## Vision

Deliver a complete, production-grade complaint resolution system for the MentorX mentoring platform. The system handles the full lifecycle — complaint filing → investigation → evidence review → mediation → resolution — with UUID-decoupled entity references (microservices principle), `@PreAuthorize` authorization on every endpoint, status transition validation, immutable admin audit logging, and Flyway-managed schema migrations.

---

## Phase 1 — Foundation (DONE)

Entity definitions, enums, controllers, and service layer scaffolding.

| Task | Status |
|------|--------|
| Create `Dispute` entity with lifecycle fields | DONE |
| Create `DisputeEvidence` entity with review/flagging workflow | DONE |
| Create `Report` entity (content moderation) | DONE |
| Create `AdminActivityLog` entity (immutable audit trail) | DONE |
| Define `DisputeStatus` enum (9 states) with `allowedTransitions` mapping | DONE |
| Define `DisputeOutcome` enum (7 values — no financial outcomes) | DONE |
| Define `ReportStatus` / `ReportTargetType` enums | DONE |
| Define `EvidenceType` enum | DONE |
| Implement `DisputeController` (8 endpoints) | DONE |
| Implement `ReportController` (6 endpoints) | DONE |
| Implement `DisputeEvidenceController` (2 endpoints) | DONE |
| Implement `DisputeService` interface | DONE |
| Implement `ReportService` interface | DONE |
| Implement `DisputeEvidenceService` interface | DONE |
| Define error codes for disputes/reports/evidence | DONE |
| Remove `IN_ARBITRATION` from `DisputeStatus` | DONE |
| Remove escrow/financial fields from `Dispute` entity | DONE |
| Remove `@ManyToOne User` joins — replace with UUID columns | DONE |
| Remove empty `feature.report` stub package | DONE |

---

## Phase 2 — Database & Schema via Flyway (IMMEDIATE NEXT)

### 2.1 Create Flyway Migrations for All Tables

Replace Hibernate DDL auto with versioned Flyway migrations for deterministic, environment-agnostic schema management. All enum columns use `VARCHAR(255)` matching `@Enumerated(STRING)`.

| Migration File | Table | Key Columns |
|---------------|-------|-------------|
| `V2.13.0__create_disputes_table.sql` | `disputes` | `complainant_id UUID NOT NULL`, `respondent_id UUID NOT NULL`, `booking_id UUID`, `status VARCHAR(50) NOT NULL DEFAULT 'OPEN'`. Indexes on `complainant_id`, `respondent_id`, `status`, `booking_id`. |
| `V2.13.1__create_dispute_evidence_table.sql` | `dispute_evidence` | `dispute_id UUID NOT NULL`, `submitted_by_user_id UUID NOT NULL`, `is_reviewed BOOLEAN DEFAULT FALSE`, `is_flagged BOOLEAN DEFAULT FALSE`. Index on `dispute_id`. |
| `V2.13.2__create_reports_table.sql` | `reports` | `reporter_id UUID NOT NULL`, `target_id UUID`, `status VARCHAR(50) NOT NULL DEFAULT 'PENDING'`, `evidence_urls JSONB`. Index on `status`. |
| `V2.13.3__create_admin_activity_logs_table.sql` | `admin_activity_logs` | `admin_id UUID NOT NULL`, `action_type VARCHAR(100) NOT NULL`, `target_type VARCHAR(50)`, `target_id UUID`, `previous_state JSONB`, `new_state JSONB`. Index on `(target_type, target_id)`. |

### 2.2 Synchronize PostgreSQL Enums

The `init-schema.sql` native enum types are stale (4 values). A Flyway migration syncs them to match Java enums. Since PostgreSQL `ALTER TYPE ... ADD VALUE` does not support `IF NOT EXISTS` pre-PG 13, the recommended approach is to drop and recreate each enum type within a transaction, or use a DO block to conditionally add missing values.

```sql
-- V2.13.4__sync_postgresql_enums.sql
-- Drops and recreates enum types to match current Java enum values.
-- Tables use VARCHAR columns with @Enumerated(STRING), so this is schema documentation, not an active constraint.

DROP TYPE IF EXISTS dispute_status CASCADE;
CREATE TYPE dispute_status AS ENUM (
    'OPEN', 'AWAITING_RESPONSE', 'INVESTIGATING', 'EVIDENCE_REVIEW',
    'IN_MEDIATION', 'RESOLVED', 'CLOSED', 'WITHDRAWN', 'EXPIRED'
);

DROP TYPE IF EXISTS dispute_outcome CASCADE;
CREATE TYPE dispute_outcome AS ENUM (
    'FAVOR_COMPLAINANT', 'FAVOR_RESPONDENT', 'COMPROMISE',
    'MUTUAL_AGREEMENT', 'INVALID_COMPLAINT', 'WARNING_ISSUED', 'NO_OUTCOME'
);

DROP TYPE IF EXISTS report_status CASCADE;
CREATE TYPE report_status AS ENUM (
    'PENDING', 'UNDER_REVIEW', 'ESCALATED', 'RESOLVED',
    'DISMISSED', 'ON_HOLD', 'CLOSED'
);

DROP TYPE IF EXISTS report_target_type CASCADE;
CREATE TYPE report_target_type AS ENUM (
    'USER_PROFILE', 'MENTOR_PROFILE', 'SESSION', 'REVIEW', 'MESSAGE',
    'COMMENT', 'COURSE', 'COURSE_CONTENT', 'PLATFORM_ISSUE'
);
```

**Note:** `CASCADE` will drop any dependent objects. Ensure migrations run in a controlled order where table creation precedes enum recreation, and tables use `VARCHAR` columns rather than native enum column types.

---

## Phase 3 — Authorization with Custom Permission Evaluators

### 3.1 Implement `DisputePermissionEvaluator`

Register a Spring bean named `disputeEvaluator` that the `@PreAuthorize` SpEL expressions reference.

```java
@Component("disputeEvaluator")
public class DisputePermissionEvaluator {
    private final DisputeRepository disputeRepository;

    // isParty(UUID disputeId, CustomUserDetails principal) — true if complainant or respondent
    // isComplainant(UUID disputeId, CustomUserDetails principal) — true if complainant
    // isRespondent(UUID disputeId, CustomUserDetails principal) — true if respondent
}
```

### 3.2 Apply `@PreAuthorize` to All Endpoints

| Endpoint | Guard |
|----------|-------|
| `POST /api/disputes` | `isAuthenticated()` |
| `GET /api/disputes/{id}` | `@disputeEvaluator.isParty(#id, authentication.principal)` |
| `GET /api/disputes/user/{userId}` | `#userId == authentication.principal.id` |
| `POST /api/disputes/{id}/respond` | `@disputeEvaluator.isRespondent(#id, authentication.principal)` |
| `POST /api/disputes/{id}/withdraw` | `@disputeEvaluator.isComplainant(#id, authentication.principal)` |
| `POST /api/disputes/{id}/assign-mediator` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| `POST /api/disputes/{id}/resolve` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| `GET /api/disputes` (filtered listing) | `hasAnyRole('MODERATOR', 'ADMIN')` |
| `POST /api/disputes/{id}/evidence` | `@disputeEvaluator.isParty(#id, authentication.principal)` |
| `GET /api/disputes/{id}/evidence` | `@disputeEvaluator.isParty(#id, authentication.principal)` |
| `POST /api/reports` | `isAuthenticated()` |
| `GET /api/reports/{id}` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| `GET /api/reports` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| `POST /api/reports/{id}/assign` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| `POST /api/reports/{id}/resolve` | `hasAnyRole('MODERATOR', 'ADMIN')` |
| `POST /api/reports/{id}/escalate` | `hasAnyRole('MODERATOR', 'ADMIN')` |

Verify that `MethodSecurityConfig` enables `@EnableMethodSecurity` (or `@EnableGlobalMethodSecurity(prePostEnabled = true)` for pre-Spring Security 6).

---

## Phase 4 — Status Transition Validation

### 4.1 Implement `allowedTransitions` on `DisputeStatus`

```java
OPEN               → AWAITING_RESPONSE, WITHDRAWN, EXPIRED
AWAITING_RESPONSE  → INVESTIGATING, WITHDRAWN, EXPIRED
INVESTIGATING     → EVIDENCE_REVIEW, IN_MEDIATION, WITHDRAWN, EXPIRED
EVIDENCE_REVIEW    → IN_MEDIATION, WITHDRAWN, EXPIRED
IN_MEDIATION       → RESOLVED, WITHDRAWN, EXPIRED
RESOLVED           → CLOSED
CLOSED, WITHDRAWN, EXPIRED → (terminal — empty set)
```

### 4.2 Add `transitionStatus()` on `Dispute` Entity

```java
public void transitionStatus(DisputeStatus newStatus) {
    if (!this.status.canTransitionTo(newStatus)) {
        throw new BusinessException(ErrorCode.INVALID_DISPUTE_STATUS,
            "Cannot transition from " + this.status + " to " + newStatus);
    }
    this.status = newStatus;
}
```

### 4.3 Apply Transition Validation in ReportStatus

```
PENDING      → UNDER_REVIEW, DISMISSED
UNDER_REVIEW → ESCALATED, RESOLVED, DISMISSED, ON_HOLD
ESCALATED    → UNDER_REVIEW, RESOLVED, DISMISSED
ON_HOLD      → UNDER_REVIEW, DISMISSED
RESOLVED     → CLOSED
DISMISSED    → CLOSED
CLOSED       → (terminal)
```

### 4.4 Write Unit Tests

Test that every invalid transition throws `INVALID_DISPUTE_STATUS` and every valid transition succeeds. Cover edge cases: terminal states cannot transition anywhere, a resolved dispute cannot go back to investigating.

---

## Phase 5 — Service Layer Enhancements (Evidence & Logs)

### 5.1 Implement `DisputeEvidenceServiceImpl`

| Method | Description |
|--------|-------------|
| `addEvidence(UUID disputeId, UUID userId, DisputeEvidenceCreateRequest request)` | Creates a `DisputeEvidence` record, validates the user is a party to the dispute, stores file metadata |
| `getEvidenceForDispute(UUID disputeId)` | Returns all evidence records for a dispute, ordered by `createdAt` |
| `reviewEvidence(UUID evidenceId, UUID reviewerId, boolean approved, String notes)` | Marks evidence as reviewed, sets `reviewedByUserId`, `reviewedAt`, `reviewNotes`. Creates `AdminActivityLog` with `actionType=EVIDENCE_REVIEWED`. |
| `flagEvidence(UUID evidenceId, String reason)` | Sets `isFlagged=true`, `flagReason`. Creates `AdminActivityLog` with `actionType=EVIDENCE_FLAGGED`. |

### 5.2 Integrate `AdminActivityLog` into Services

Inject `AdminActivityLogRepository` into `DisputeServiceImpl`, `ReportServiceImpl`, and `DisputeEvidenceServiceImpl`. Create a private helper `logModerationAction(...)` in each to reduce boilerplate.

| Trigger | actionType | actionCategory |
|---------|-----------|----------------|
| `assignMediator()` | `DISPUTE_MEDIATOR_ASSIGNED` | `MODERATION` |
| `resolve()` | `DISPUTE_RESOLVED` | `MODERATION` |
| `assignReport()` | `REPORT_ASSIGNED` | `CONTENT_MANAGEMENT` |
| `resolveReport()` | `REPORT_RESOLVED` | `CONTENT_MANAGEMENT` |
| `reviewEvidence()` | `EVIDENCE_REVIEWED` | `CONTENT_MANAGEMENT` |
| `flagEvidence()` | `EVIDENCE_FLAGGED` | `CONTENT_MANAGEMENT` |

Each log entry captures: `adminId`, `actionType`, `targetType`, `targetId`, `affectedUserId`, `previousState` (JSONB), `newState` (JSONB), `description`, and `createdAt`.

---

## Cold-Start Resumption

To resume work on this feature:

1. Read `complaint.introduction.md` for context, lifecycle, and current risks.
2. Read `complaint.hallucination.md` for the architectural rules audit — what was intentionally removed and why.
3. Read `complaint.sourcecode.md` for exact file locations, entity schemas, and enum definitions.
4. Read `complaint.useguide.md` for the API contract including `@PreAuthorize` guards and response schemas.
5. Start with **Phase 2** — create Flyway migrations and sync PostgreSQL enums.
6. Proceed to **Phase 3** — implement `DisputePermissionEvaluator` and apply `@PreAuthorize` annotations.
7. Then **Phase 4** — implement status transition validation on `DisputeStatus` and `ReportStatus`.
8. Finally **Phase 5** — wire up `DisputeEvidenceService` and `AdminActivityLog` integration.
