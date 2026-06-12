# Complaint (Khiếu nại) Feature — Introduction

## Purpose

The Complaint feature enables users (both mentors and mentees) on the MentorX **mentoring platform** to formally dispute session outcomes, conduct issues, or platform interactions. It provides a structured escalation path from initial complaint through investigation, evidence review, mediation, and resolution — all scoped strictly to mentoring engagements.

## Architecture Context

MentorX uses a **microservices architecture**. The complaint feature operates within its own bounded context and is **decoupled from the User entity** — all person references (complainant, respondent, mediator, admin) are UUID columns only. No JPA `@ManyToOne` joins to a shared User entity exist. Cross-service resolution (names, avatars) happens at the API gateway or via a dedicated User Profile service, not through entity relationships.

## Mentoring Platform Scope

The complaint system covers disputes specific to mentoring engagements:

- **Session quality disputes** — mentor no-show, inadequate preparation, misleading materials
- **Conduct/communication disputes** — unprofessional behavior, harassment, breach of platform code of conduct
- **Scheduling disputes** — repeated cancellations, failure to honor booked sessions
- **Misrepresentation disputes** — credentials claimed vs actual, portfolio authenticity
- **Platform policy violations** — any breach of the MentorX terms of service within a mentoring context

**Strictly out of scope** for this module:

- **Financial/escrow handling** — refunds, disputed amounts, and escrow are the responsibility of the Payments microservice. This module stores only a nullable `bookingId` reference for correlation.
- **Third-party binding arbitration** — mentoring disputes result in platform actions (warnings, suspensions, bans), not monetary judgments. There is no `IN_ARBITRATION` status, no arbitrator role, and no `escalateToArbitration` pathway.
- **Contract/cancellation disputes** — handled by the Bookings/Sessions microservice via the `sessionId` or `bookingId` reference.

## Key Concepts

| Term | Definition |
|------|-----------|
| **Complainant** | User (mentor or mentee) who files the complaint — stored as UUID |
| **Respondent** | User against whom the complaint is filed — stored as UUID |
| **Mediator** | Platform moderator assigned to facilitate resolution — stored as UUID |
| **Dispute** | Formal complaint record with lifecycle, evidence, and outcome |
| **Report** | Content-moderation report for abusive content (co-located in the same module) |
| **DisputeEvidence** | Fully implemented entity with its own table, CRUD service, review/flagging workflow, and dedicated API endpoints. Evidence is never stored in generic JSONB. |
| **AdminActivityLog** | Immutable audit trail written to on every moderator/admin-triggered state change (assign mediator, resolve, review evidence, flag evidence, assign/report resolve). User-initiated actions (filing, responding, uploading evidence) are tracked via entity timestamps. |

## Complaint Lifecycle (9 States)

```
OPEN → AWAITING_RESPONSE, WITHDRAWN, EXPIRED
AWAITING_RESPONSE → INVESTIGATING, WITHDRAWN, EXPIRED
INVESTIGATING → EVIDENCE_REVIEW, IN_MEDIATION, WITHDRAWN, EXPIRED
EVIDENCE_REVIEW → IN_MEDIATION, WITHDRAWN, EXPIRED
IN_MEDIATION → RESOLVED, WITHDRAWN, EXPIRED
RESOLVED → CLOSED
CLOSED, WITHDRAWN, EXPIRED are terminal states (no outward transitions)
```

## Current Implementation Status (2026-06-12)

| Component | Status |
|-----------|--------|
| `Dispute` entity | Complete — table `disputes`, UUID-decoupled |
| `DisputeEvidence` entity | Complete — table `dispute_evidence`, fully wired into service layer |
| `Report` entity | Complete — table `reports`, content moderation, UUID-decoupled |
| `AdminActivityLog` entity | Complete — table `admin_activity_logs`, populated on every admin state change |
| DisputeController (`/api/disputes`) | Complete — all endpoints `@PreAuthorize`-guarded |
| ReportController (`/api/reports`) | Complete — all endpoints `@PreAuthorize`-guarded |
| DisputeEvidenceController | Complete — upload and list evidence per dispute |
| DisputeService | Complete — create, respond, withdraw, assign mediator, resolve, with status transition validation |
| ReportService | Complete — create, assign, resolve, escalate, with status transition validation |
| DisputePermissionEvaluator | Complete — custom SpEL evaluator for `isParty`, `isComplainant`, `isRespondent` |
| DisputeEvidenceService | Complete — add, list, review, flag evidence |
| AdminActivityLog integration | Complete — written to by DisputeServiceImpl and ReportServiceImpl on every admin action |
| Status transition validation | Complete — `Dispute` entity validates transitions via `allowedTransitions` map |
| Flyway migrations for all tables | Complete — `disputes`, `dispute_evidence`, `reports`, `admin_activity_logs` |
| PostgreSQL enum sync via Flyway | Complete — enums match Java `DisputeStatus`, `DisputeOutcome`, `ReportStatus`, `ReportTargetType` |
| Arbitration logic | Removed entirely |
| Escrow/financial fields | Removed from Dispute entity; financial disputes reference `bookingId` only |

## Open Risks

1. S3 file upload endpoint for evidence files needs a presigned-URL integration or direct multipart upload handler.
2. No notification integration — push/email on complaint status changes is deferred.
3. Auto-expiry of stale disputes (`@Scheduled` task) is not yet implemented.
4. Gateway-layer user name/avatar resolution for dispute listings is not yet wired (currently resolved via API call from the service layer).
