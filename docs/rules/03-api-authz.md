# 03 API Authz

## Purpose
Set strict backend authorization rules for Mentor X APIs, especially around sensitive auth, contract, wallet, and notification paths.

## Core Rules
- No sensitive API may trust caller-supplied `userId`.
- Sensitive actions must resolve the current user from the security context.
- Role checks do not replace ownership checks.
- Ownership checks do not replace role checks when elevated permission is required.
- Notification APIs must require authentication unless a route is intentionally public and documented.

## Current User Enforcement
- Use the security context as the source of truth for who is acting.
- Request DTOs may contain IDs for related resources, but not to prove actor identity.
- Service methods that mutate sensitive state must re-check actor identity even if the controller is annotated.

Example:

```java
UUID actorId = securityService.getCurrentUserId();
if (!resource.getOwner().getId().equals(actorId)) {
    throw accessDenied();
}
```

## Sensitive Operations That Must Not Trust Request `userId`
- logout all sessions
- change password
- enable or disable 2FA
- notification inbox access
- wallet reads and writes
- withdrawal requests
- contract sign, activate, cancel, complete
- proposal accept or reject
- negotiation accept or reject

## Ownership Rules
- Users can only manage their own profile, jobs, notifications, wallet, and security settings.
- Mentors can only manage their own mentor resources unless admin is acting.
- Contract actions must be limited to contract participants or admins where explicitly allowed.
- Proposal actions must be limited to the owning mentor, the job owner, or an approved admin path.

## Role Rules
- `ADMIN`
  - full financial and system governance
- `MODERATOR`
  - moderation only
  - no balance edits
  - no withdrawal approval
- `MENTOR`
  - approved mentor actions only
- `USER`
  - standard client actions only

## Notification Rules
- Notification read endpoints require authentication.
- Notification list and unread count must not accept arbitrary target users without admin rights.
- Notification send endpoints must not be public.

## Contract Participant Rules
- Client-only:
  - accept mentor
  - complete contract
  - request cancellation
- Mentor-only:
  - mentor-side response actions that are explicitly defined
- Admin-only:
  - exceptional support or financial interventions, if implemented

## Do
- Keep `@PreAuthorize` and service-level checks aligned.
- Re-check authorization inside services for critical mutations.
- Treat financial and auth endpoints as high-risk by default.

## Do Not
- Use `@PreAuthorize("isAuthenticated()")` alone for sensitive mutations.
- Expose inbox or wallet state by arbitrary `userId`.
- Assume frontend route guards are enough.

## MVP Scope
- Safe ownership and RBAC enforcement for all production-facing APIs.

## Future Scope / TODO
- Introduce centralized authz helper patterns where duplication is high, but do not weaken service checks.
