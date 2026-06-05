# 01 Account Role Model

## Purpose
Define the account lifecycle and RBAC boundaries for Mentor X backend logic.

## Core Rules
- Every new account starts as `USER`.
- Mentor is an additive approved role and mode, not a separate account type.
- `ADMIN` and `MODERATOR` are RBAC roles, not alternative account systems.
- Role checks and mentor approval checks are separate concerns and may both be required.

## USER-First Lifecycle
1. User registers.
2. Backend creates a `USER` account.
3. Email verification or approved auth flow activates the account.
4. User may apply for mentor approval.
5. After approval, the same account becomes `USER + MENTOR`.

## Role Boundaries
- `USER`
  - browse marketplace
  - create jobs
  - receive proposals
  - negotiate
  - accept mentor
  - own contracts as client
- `MENTOR`
  - available only after approval
  - manage mentor profile and mentor workspace
  - submit proposals
  - negotiate
  - fulfill contracts
  - request withdrawals subject to payout rules
- `MODERATOR`
  - review and moderate content, reports, mentor applications, disputes, abuse signals
  - must not approve withdrawals
  - must not edit balances
  - must not perform hidden financial overrides
- `ADMIN`
  - full system governance
  - approve or reject withdrawals
  - manage refunds, fees, roles, settings, and financial controls

## Mentor Mode Rules
- Mentor access requires approved mentor status, not only the presence of a `MENTOR` role.
- Backend checks must prevent unapproved mentors from using mentor-only business actions.
- User mode switching in clients must never replace backend enforcement.

## Financial Restrictions
- Moderator cannot approve withdrawals.
- Moderator cannot issue refunds directly.
- Moderator cannot edit wallet balances.
- Only admin-approved backend flows can mutate financial state.

## Do
- Check current authenticated user and role membership from security context.
- Check mentor approval state for mentor content and mentor financial actions.
- Keep account identity stable across roles.

## Do Not
- Create a separate mentor account table or separate login identity.
- Assume `isMentor` alone is enough for mentor access.
- Give moderator hidden finance powers.

## MVP Scope
- Stable USER-first lifecycle with additive mentor access.
- Clear separation of admin moderation and admin finance responsibilities.

## Future Scope / TODO
- Formalize account suspension, freeze, and recovery policy with dedicated docs when needed.
