# 12 Testing Build

## Purpose
Define the minimum verification standard for Mentor X backend changes.

## Core Rules
- Every meaningful backend change must compile before handoff.
- Sensitive flows require targeted manual verification even when tests exist.
- Financial and authz changes require higher scrutiny than cosmetic refactors.

## Required Build Command
```powershell
./mvnw -q -DskipTests compile
```

On Windows, `mvnw.cmd` is also acceptable if needed by the environment.

## When To Run Tests
- Run compile on every backend code change before reporting completion.
- Run targeted tests when changing:
  - auth
  - wallet
  - escrow
  - contracts
  - negotiation
  - notification authz
- Run broader integration coverage when lifecycle or persistence behavior changes.

## Manual Verification Checklist
- Authz
  - sensitive endpoints do not trust request `userId`
  - unauthorized users cannot read another user's notifications or wallet
  - moderator cannot approve withdrawals
- Negotiation
  - offer uses only `price + deadlineAt + message`
  - accepting terms does not create contract
  - accepting terms does not lock escrow
- Contract
  - client acceptance creates contract
  - contract creation locks escrow once
  - participant checks are enforced
- Escrow
  - completion releases funds once
  - refund does not double-run
  - dispute keeps funds locked
- Config
  - no new secrets are committed
  - no unsafe defaults are introduced

## Do
- State what was built, what was compiled, and what was not verified.
- Prefer targeted verification around touched modules.

## Do Not
- Skip compile after backend edits.
- Claim flows are safe without checking authz and financial paths.
- Hide missing tests.

## MVP Scope
- Reliable build validation plus focused manual checks on high-risk flows.

## Future Scope / TODO
- Add module-specific test suites and CI gates as the backend stabilizes.
