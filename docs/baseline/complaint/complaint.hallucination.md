# Complaint (Khiếu nại) Feature — Architectural Rules Audit

This document records verified architectural decisions, intentional design exclusions, and the rationale behind each. It serves as a guardrail against drift — any future change that reintroduces a concept listed in §3 (Intentionally Removed) should be treated as a regression.

---

## 1. Verified Architectural Rules

These rules are enforced in the current target state and must be preserved.

| Rule | Rationale | Enforcement |
|------|-----------|-------------|
| All person references are UUID columns, not `@ManyToOne User` | Microservices boundaries — the Dispute module must not share entity classes with the User module. Cross-service name/avatar resolution happens at the gateway. | Entity schema, Flyway migrations (no FK constraints to `users` table) |
| `DisputeEvidence` is a standalone entity with its own table and service | JSONB blobs are opaque, unqueryable, and bypass the review/flagging workflow. The entity provides structured evidence tracking with `isReviewed`, `isFlagged`, and reviewer attribution. | `DisputeEvidence.java`, `DisputeEvidenceService.java`, `dispute_evidence` table |
| `AdminActivityLog` is written on every admin-triggered state change | Immutable audit trail is required for compliance. User-initiated actions (filing, responding, uploading) are tracked via entity timestamps and are not logged to `AdminActivityLog`. | `DisputeServiceImpl`, `ReportServiceImpl`, `DisputeEvidenceServiceImpl` inject `AdminActivityLogRepository` |
| Status transitions must be validated via `allowedTransitions` map | Unvalidated any→any transitions can corrupt complaint state (e.g., a CLOSED dispute being reopened). The `DisputeStatus` enum owns the transition rules. | `Dispute.transitionStatus()` method, `DisputeStatus.canTransitionTo()` |
| `@PreAuthorize` guards on every endpoint | No endpoint may be open to unauthorized access. Ownership checks use the custom `disputeEvaluator` SpEL bean; role checks use `hasAnyRole('MODERATOR', 'ADMIN')`. | All controller methods, verified by `DisputePermissionEvaluator` |
| `bookingId` is the sole financial reference | The complaint module does not handle escrow, refunds, or monetary amounts. If a dispute involves payment, the Payments service resolves it independently using the `bookingId` for correlation. | `Dispute` entity — no financial fields |

---

## 2. Design Decisions Record

| Decision | Rationale | Date |
|----------|-----------|------|
| Evidence stored in `DisputeEvidence` entity, not JSONB `metadata` | Structured storage enables querying by evidence type, review status, and submitter. JSONB is opaque and cannot be indexed for these queries. | 2026-06-12 |
| `AdminActivityLog` for admin actions only | User actions are self-service and don't require an immutable compliance audit. Entity timestamps and status changes track user behavior. | 2026-06-12 |
| Gateway-layer user name resolution (not JPA join) | Decoupling the Dispute module from the User entity enables independent deployment and scaling. The gateway aggregates profile data from the User Profile service. | 2026-06-12 |
| 9-state lifecycle (no `IN_ARBITRATION`) | Arbitration implies binding third-party monetary judgments, which are not applicable to a mentoring platform. Disputes resolve at mediation with platform-appropriate outcomes. | 2026-06-12 |
| `@Scheduled` task for auto-expiry | A daily scheduled job is simpler than database triggers and more maintainable than event-driven expiry for this scale. It queries OPEN disputes past their `responseDeadline` and transitions them to EXPIRED. | 2026-06-12 |
| Flyway managed migrations (not Hibernate DDL auto) | Hibernate DDL auto is not deterministic across environments and can't handle enum synchronization. Flyway provides versioned, repeatable schema changes. | 2026-06-12 |

---

## 3. Intentionally Removed — Do Not Reintroduce

These concepts were present in earlier versions of the codebase and have been deliberately stripped. Reintroducing any of them constitutes architectural regression.

### 3.1 Arbitration

**What was removed:**
- `DisputeStatus.IN_ARBITRATION` enum value (10 → 9 states)
- `Dispute` entity fields: `arbitratorId`, `arbitrationStartedAt`, `requiresArbitration`
- `escalateToArbitration()` method on `Dispute`
- Any transition path through `IN_ARBITRATION`

**Why:** Arbitration is a binding third-party process with monetary enforcement — it belongs to financial dispute systems (escrow platforms, payment gateways), not a mentoring platform. The MentorX complaint system resolves disputes with mentoring-appropriate outcomes: warnings, suspensions, bans, or behavioral agreements reached via mediation.

### 3.2 Escrow / Financial Handling

**What was removed:**
- `Dispute` entity fields: `fundsInEscrow`, `escrowRecordId`, `disputedAmountMxc`, `refundRequestedMxc`, `refundAmountMxc`
- `DisputeOutcome` values: `FULL_REFUND`, `PARTIAL_REFUND`, `NO_REFUND`
- Any service logic that updates wallet/escrow state

**Why:** Escrow and payment processing are the exclusive domain of the Payments microservice. The complaint module references the booking via `bookingId` only. Refunds, holds, and releases are handled independently by Payments — the complaint module's resolution does not trigger financial operations.

### 3.3 Direct JPA `@ManyToOne` User Joins

**What was removed:**
- `@ManyToOne(fetch = LAZY) User initiator` on `Dispute` → `UUID complainantId`
- `@ManyToOne(fetch = LAZY) User respondent` on `Dispute` → `UUID respondentId`
- `@ManyToOne(fetch = LAZY) User mediator` on `Dispute` → `UUID mediatorId`
- `@ManyToOne User submittedByUser` on `DisputeEvidence` → `UUID submittedByUserId`
- `@ManyToOne User reporter` on `Report` → `UUID reporterId`
- `@ManyToOne User admin` on `AdminActivityLog` → `UUID adminId`
- `DisputeServiceImpl` dependency on `UserRepository` → removed

**Why:** In a microservices architecture, bounded contexts must not share entity classes. A `@ManyToOne` join couples the moderation module's database schema to the user module's schema, preventing independent deployment and schema evolution. UUID references with gateway-layer resolution are the standard microservices pattern.

### 3.4 Generic JSONB `metadata` / `evidenceUrls` for Evidence

**What was removed:**
- `metadata` JSONB column on `Dispute` (used for evidence storage)
- `evidenceUrls` JSONB column on `Dispute` (list of URLs)

**Why:** JSONB is opaque and unqueryable — you can't index it to find all evidence submitted by a user, or all flagged evidence, or all unreviewed evidence. The `DisputeEvidence` entity provides a structured table with review/flagging workflow, submitter tracking, and proper indexing.

### 3.5 Empty Stub Package

**What was removed:**
- `com.mentorx.api.feature.report/` — contained only an empty `report.java` stub

**Why:** The actual `Report` entity, controller, and service live in the `feature.moderation` package. The stub package had no value and created confusion about where report logic resides.

---

## 4. Known Schema Drift (Resolved)

The `init-schema.sql` PostgreSQL native enums were historically stale (4 values each vs 7-10 in Java). This has been resolved via Flyway migrations that drop and recreate the enum types with the full value set. The tables use `VARCHAR` columns with `@Enumerated(STRING)`, so the PostgreSQL enum types serve as schema documentation rather than active constraints.

| Enum | Old (init-schema.sql) | Current (Java + Flyway) |
|------|----------------------|------------------------|
| `dispute_status` | OPEN, REVIEWING, RESOLVED, CLOSED (4) | OPEN, AWAITING_RESPONSE, INVESTIGATING, EVIDENCE_REVIEW, IN_MEDIATION, RESOLVED, CLOSED, WITHDRAWN, EXPIRED (9) |
| `dispute_outcome` | FAVOR_CLIENT, FAVOR_MENTOR, PARTIAL_REFUND, NO_ACTION (4) | FAVOR_COMPLAINANT, FAVOR_RESPONDENT, COMPROMISE, MUTUAL_AGREEMENT, INVALID_COMPLAINT, WARNING_ISSUED, NO_OUTCOME (7) |
| `report_status` | PENDING, REVIEWING, RESOLVED, DISMISSED (4) | PENDING, UNDER_REVIEW, ESCALATED, RESOLVED, DISMISSED, ON_HOLD, CLOSED (7) |
| `report_target_type` | USER, JOB, COURSE, MESSAGE, REVIEW (5) | USER_PROFILE, MENTOR_PROFILE, SESSION, REVIEW, MESSAGE, COMMENT, COURSE, COURSE_CONTENT, PLATFORM_ISSUE (9) |

---

## 5. Validation Checklist

Before marking this feature complete, verify:

- [ ] No `@ManyToOne` to `User` exists in any entity under `feature.moderation`
- [ ] `DisputeStatus` has exactly 9 values with `allowedTransitions` populated
- [ ] `DisputeOutcome` has exactly 7 values (no refund/financial outcomes)
- [ ] `Dispute` entity has zero escrow/arbitration fields
- [ ] `DisputeEvidence` is used by the service layer (no evidence in JSONB)
- [ ] `AdminActivityLog` is written on every `assignMediator`, `resolve`, `assignReport`, `resolveReport`, `reviewEvidence`, `flagEvidence` call
- [ ] All controller methods have `@PreAuthorize` annotations matching the API contract
- [ ] `DisputePermissionEvaluator` is registered as a Spring bean named `disputeEvaluator`
- [ ] Flyway migrations exist for all 4 tables (`disputes`, `dispute_evidence`, `reports`, `admin_activity_logs`)
- [ ] `init-schema.sql` enums are synced with Java enum values
