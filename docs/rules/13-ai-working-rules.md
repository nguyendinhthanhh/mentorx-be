# 13 AI Working Rules

## Purpose
Define how AI assistants must work inside the Mentor X backend repository.

## Core Rules
- Read the relevant rules first.
- Inspect current implementation before changing code.
- Explain the current behavior before proposing a fix.
- Propose the smallest safe change that solves the real issue.
- Change only files related to the task.
- Run build or tests before final handoff when code changes are made.

## Required AI Workflow
1. Read `/docs/rules/README.md` and the relevant rule files.
2. Inspect the related backend files.
3. Summarize the current implementation and risk.
4. Propose a minimal safe change.
5. Modify only related files.
6. Run build and relevant tests.
7. Report changed files and remaining risks.

## Specific Mentor X Rules For AI
- Do not rewrite the whole project.
- Do not add fake data.
- Do not silently change business flow.
- Do not move financial logic into controllers or mappers.
- Do not trust frontend assumptions over backend rules.
- Do not hide errors, broken assumptions, or incomplete verification.

## Change Scope Rule
- If the issue is local, keep the change local.
- If the issue crosses authz and finance boundaries, say so explicitly before broadening scope.

## Reporting Rule
- Final report must include:
  - what changed
  - why it changed
  - what was verified
  - any remaining risks or follow-up items

## Do
- Be explicit about assumptions.
- Preserve project-specific structure.
- Prefer existing patterns when they are still correct.

## Do Not
- Generate generic architecture rewrites.
- Add broad refactors without request.
- Pretend an unverified financial or auth flow is safe.

## MVP Scope
- Controlled, reviewable AI assistance for a risky backend domain.

## Future Scope / TODO
- Add a PR template section requiring confirmation that these AI rules were followed.
