# Mentor X Backend Rules

## Purpose
This folder defines the operating rules for the Mentor X backend. Any AI agent, developer, reviewer, or contributor must read these rules before changing backend code, database structure, auth flows, wallet logic, contract logic, or runtime configuration.

These rules exist because the current backend already contains product drift and risk areas:
- authz paths that rely too much on request data
- negotiation fields that exceed the confirmed MVP
- wallet and escrow logic with multiple state surfaces
- mock or demo behavior that must not leak into production APIs
- config defaults and secrets that must be tightened

## Core Rules
- Read `/docs/rules` before making backend changes.
- Product rules override convenience, legacy UI assumptions, and generated code habits.
- Business logic belongs in services, not controllers or mappers.
- Financial safety is more important than speed of implementation.
- Do not silently change business flow, authz rules, or financial semantics.

## Recommended Reading Order
1. [00-product-foundation.md](</D:/Mentor X/mentorx-be/docs/rules/00-product-foundation.md>)
2. [01-account-role-model.md](</D:/Mentor X/mentorx-be/docs/rules/01-account-role-model.md>)
3. [02-business-flow.md](</D:/Mentor X/mentorx-be/docs/rules/02-business-flow.md>)
4. [03-api-authz.md](</D:/Mentor X/mentorx-be/docs/rules/03-api-authz.md>)
5. [06-negotiation-mvp.md](</D:/Mentor X/mentorx-be/docs/rules/06-negotiation-mvp.md>)
6. [05-contract-lifecycle.md](</D:/Mentor X/mentorx-be/docs/rules/05-contract-lifecycle.md>)
7. [04-wallet-escrow.md](</D:/Mentor X/mentorx-be/docs/rules/04-wallet-escrow.md>)
8. [07-backend-code-structure.md](</D:/Mentor X/mentorx-be/docs/rules/07-backend-code-structure.md>)
9. [08-mapper-rules.md](</D:/Mentor X/mentorx-be/docs/rules/08-mapper-rules.md>)
10. [09-database-rules.md](</D:/Mentor X/mentorx-be/docs/rules/09-database-rules.md>)
11. [10-secrets-config-rules.md](</D:/Mentor X/mentorx-be/docs/rules/10-secrets-config-rules.md>)
12. [11-no-fake-data.md](</D:/Mentor X/mentorx-be/docs/rules/11-no-fake-data.md>)
13. [12-testing-build.md](</D:/Mentor X/mentorx-be/docs/rules/12-testing-build.md>)
14. [13-ai-working-rules.md](</D:/Mentor X/mentorx-be/docs/rules/13-ai-working-rules.md>)
15. [14-cleanup-roadmap.md](</D:/Mentor X/mentorx-be/docs/rules/14-cleanup-roadmap.md>)

## How To Use These Rules In Future Prompts
- State that the task is for the `mentorx-be` repository.
- Require the agent to read `/docs/rules/README.md` and the relevant rule files first.
- Name the module being changed, such as `auth`, `job`, `wallet`, or `notification`.
- Require the agent to inspect the current implementation before proposing any code change.
- Require the agent to explain current behavior before proposing code changes.
- Require minimal safe changes only.

Example prompt:

```md
Read `/docs/rules/README.md`, `03-api-authz.md`, and `04-wallet-escrow.md` first.
Inspect the current wallet withdrawal flow in `mentorx-be`.
Summarize the current implementation.
Propose the smallest safe fix.
Do not change unrelated files.
Run the required build command.
```

## Do
- Treat this folder as mandatory project guidance.
- Update the relevant rule file when business policy changes.
- Use these rules during code review and AI-assisted implementation.

## Do Not
- Skip the rule read before implementation.
- Override these rules with frontend assumptions.
- Treat old fields or legacy endpoints as product truth.

## MVP Scope
- Establish a stable backend policy baseline for Mentor X.
- Reduce architecture drift and risky AI-generated changes.

## Future Scope / TODO
- Add links from the main backend `README.md` to this folder.
- Add a PR checklist that references these rules.
