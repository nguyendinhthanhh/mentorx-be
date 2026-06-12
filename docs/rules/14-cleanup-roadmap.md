# 14 Cleanup Roadmap

## Purpose
Convert known backend audit findings into an execution order that reduces risk without losing product alignment.

## Core Rules
- Fix policy and documentation first.
- Fix security and authz before polishing.
- Normalize business flow before broad cleanup.
- Do not mix financial fixes with unrelated refactors.

## Phase 1: Rules And Documentation
- Finalize the rules in this folder.
- Align team and AI usage with these documents.
- Mark product truth clearly for negotiation, contracts, escrow, and RBAC.

## Phase 2: Security And Authz Fixes
- close public notification exposure
- remove trust in caller-supplied `userId` for sensitive actions
- tighten contract create, sign, activate, and participant checks
- review admin and moderator boundary enforcement

## Phase 3: Negotiation Contract Escrow Normalization
- reduce MVP negotiation surface to `price + deadlineAt + message`
- separate agreed-terms state from contract creation clearly
- define one canonical escrow truth path
- make release and refund idempotent

## Phase 4: Fake Demo Mock Cleanup
- remove production-facing mock dashboard values
- remove or lock down demo endpoints and sample runtime behavior
- ensure sample data is not enabled accidentally

## Phase 5: Onboarding Cleanup
- if backend onboarding remains active, align API behavior and persistence rules clearly
- remove split-brain or overlapping onboarding assumptions where possible

## Phase 6: Dead Code And Config Cleanup
- review stale schema bootstrap paths
- remove conflicting or obsolete compatibility paths when safe
- clean unsafe config defaults and committed secret patterns

## Phase 7: I18n And API Code Alignment
- ensure backend returns statuses and codes, not translated labels
- remove response assumptions that belong to frontend presentation
- keep API shape aligned with confirmed product rules

## Phase 8: Polish And Tests
- expand test coverage around authz and financial flows
- add regression checks for negotiation, contract, and escrow behavior
- tighten docs, comments, and build hygiene

## Do
- Execute phases in order unless a production incident requires different priority.
- Keep each phase scoped and reviewable.
- Re-run risk review after each major finance or authz phase.

## Do Not
- Start with UI-driven cleanup while authz remains weak.
- Hide financial logic issues behind refactors.
- Treat placeholder behavior as acceptable long term.

## MVP Scope
- A realistic cleanup sequence for stabilizing the Mentor X backend.

## Future Scope / TODO
- Track each phase with concrete tickets once implementation begins.
