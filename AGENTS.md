# AGENTS.md

## 1. Project Identity
Mentor X backend is a modular monolith for a mentor marketplace platform.

Stack:
- Java 21
- Spring Boot 3.2
- Spring Security with JWT and OAuth2
- Spring Data JPA
- PostgreSQL
- WebSocket
- Mail
- Redis-ready cache

Expected backend structure:
- `controller`
- `service`
- `service.impl`
- `repository`
- `entity`
- `dto.request`
- `dto.response`
- `mapper`
- `exception`
- `validation` if needed

This repository is not a generic freelance platform backend. It is the backend for a mentor marketplace with jobs, proposals, negotiation, contracts, escrow, wallet, reviews, chat, notifications, moderation, and mentor approval flows.

## 2. Mandatory Reading Before Any Change
Before changing any code, AI must read:
1. `/docs/rules/README.md`
2. `/docs/rules/00-product-foundation.md`
3. `/docs/rules/01-account-role-model.md`
4. `/docs/rules/02-business-flow.md`
5. The feature-specific rule file related to the task
6. `/docs/rules/13-ai-working-rules.md`

For backend-specific tasks, also read:
- `/docs/rules/03-api-authz.md`
- `/docs/rules/04-wallet-escrow.md`
- `/docs/rules/05-contract-lifecycle.md`
- `/docs/rules/06-negotiation-mvp.md`
- `/docs/rules/07-backend-code-structure.md`
- `/docs/rules/08-mapper-rules.md`
- `/docs/rules/09-database-rules.md`
- `/docs/rules/10-secrets-config-rules.md`
- `/docs/rules/11-no-fake-data.md`

If the task touches build or verification, also read:
- `/docs/rules/12-testing-build.md`

If the task touches cleanup, migration, or broader scope planning, also read:
- `/docs/rules/14-cleanup-roadmap.md`

No coding should begin before this reading step is complete.

## 3. Backend Architecture Rules
- Controllers must be thin.
- Services contain business logic.
- Repositories handle data access only.
- DTOs are required when API contracts expect DTOs.
- Entities must not be exposed directly when DTOs are the intended API shape.
- Validation belongs in request validation and service-layer business checks where appropriate.
- Do not move business rules into controllers just because it is faster.
- Do not move policy into repositories.

Practical rule:
- Controller: parse request, call service, return response.
- Service: validate ownership, validate roles, enforce status transitions, orchestrate domain behavior.
- Repository: query and persist.

## 4. Business Rules Summary
- Mentor X is a mentor marketplace.
- Every account starts as `USER`.
- Mentor is `USER + MENTOR` after approval, not a separate account.
- `ADMIN` and `MODERATOR` are RBAC roles.
- Moderator must not approve withdrawals or edit balances.
- Admin handles financial controls, withdrawals, refunds, platform fees, roles, and system settings.
- MentorHub is a workspace, not a duplicate marketplace.

Negotiation MVP:
- `Price`
- `Deadline date/time`
- `Message / Work details`

Do not use as default MVP negotiation terms:
- Delivery days
- Estimated delivery
- Sessions
- Separate Scope
- Separate Deliverables

Lifecycle rules:
- Accept offer terms does not create contract.
- Accept offer terms does not lock escrow.
- Contract is created only when client accepts mentor.
- Escrow is locked only when contract is created.
- Contract completion releases escrow to mentor.
- Dispute keeps escrow locked.

Backend responses:
- Return status and error codes, not translated UI labels.

## 5. Security and Authorization Rules
- Backend must enforce current-user ownership and role checks.
- No sensitive API should trust caller-supplied `userId`.
- Current actor identity must come from the security context.
- Role checks do not replace ownership checks.
- Ownership checks do not replace role checks where elevated privilege is required.

High-risk endpoints and flows:
- auth and account security actions
- notifications
- proposal acceptance and rejection
- negotiation acceptance and rejection
- contract create, sign, activate, complete, cancel, refund, dispute
- wallet access and mutation
- withdrawals and approvals
- admin and moderator operations

Specific rules:
- Notification APIs must not be public unless explicitly documented and justified.
- Contract participant authorization must be explicit.
- Mentor-only actions must still verify approved mentor access where required.
- Moderator cannot perform financial approvals.
- Admin-only finance actions must remain admin-only.

## 6. Wallet / Escrow / Financial Safety Rules
- No direct balance edits from frontend or normal business APIs.
- Do not modify wallet or escrow logic without transaction safety.
- Every financial mutation must be auditable.
- Escrow lock, release, and refund flows must be idempotent.
- Contract creation is the event that locks escrow.
- Completion is the event that releases escrow.
- Dispute keeps escrow locked until explicit resolution.
- Refund and release paths must not both run for the same funds.
- Do not add financial shortcuts just to satisfy UI expectations.

If touching financial logic, inspect:
- wallet service implementation
- escrow service implementation
- contract lifecycle implementation
- related transaction and record models

## 7. Mapper Rules
- Mappers only convert `Entity <-> DTO` or equivalent simple model shapes.
- Mappers must not contain business logic.
- Mappers must not call repositories or services.
- Mappers must not calculate wallet, escrow, payout, or permission state.
- Mappers must not enforce ownership or authorization.
- Mappers must not translate UI text.

If a response needs derived permission flags or workflow flags:
- calculate them in the service layer
- then map them into the response object

## 8. Database and Migration Rules
- PostgreSQL is the target application database.
- Prefer explicit migrations for schema changes.
- Do not rely on unsafe schema hacks as a long-term strategy.
- Preserve financial and lifecycle safety with constraints and indexes where appropriate.
- Backward compatibility matters, but must not silently expand product scope.
- If old fields remain for compatibility, treat them as legacy baggage, not active product truth.

If touching persistence, inspect:
- migration files
- runtime initialization logic
- constraints and uniqueness assumptions
- contract, proposal, wallet, escrow, and withdrawal tables

## 9. No Fake Data Rule
- No fake, demo, or mock data in production-facing APIs.
- Do not add fake balances, fake notifications, fake recommendations, fake activity, or fake chat state.
- Dev seed data is allowed only in clearly controlled development and testing paths.
- Production-facing APIs must tell the truth, even if the feature is incomplete.

## 10. AI Working Process
Before coding:
1. Read `AGENTS.md`.
2. Read relevant `/docs/rules` files.
3. Inspect related source files.
4. Summarize the current implementation.
5. Summarize the rules that apply.
6. Propose the smallest safe change.
7. Only then modify code.

After coding:
1. Run backend build:
   `mvnw -q -DskipTests compile`
2. Report changed files.
3. Report business logic impact.
4. Report security impact.
5. Report remaining risks or TODOs honestly.

Default coding posture:
- minimal safe changes
- no unrelated refactor
- no hidden business-flow changes
- explicit discussion of risk when touching authz or money

## 11. Build / Test Requirements
Required backend build after code changes:

```powershell
./mvnw -q -DskipTests compile
```

If the environment prefers Windows launcher:

```powershell
./mvnw.cmd -q -DskipTests compile
```

When relevant, also review:
- `/docs/rules/12-testing-build.md`

Minimum reporting after changes:
- build result
- what was verified
- what was not verified
- why any skipped verification remains

## 12. Forbidden Behaviors
- Do not rewrite the whole project.
- Do not refactor unrelated modules.
- Do not change business logic silently.
- Do not add fake data.
- Do not expose entities directly if DTOs are expected.
- Do not put business logic in mappers.
- Do not trust caller-supplied `userId` for sensitive operations.
- Do not modify wallet or escrow logic without transaction safety.
- Do not hide errors just to make build pass.
- Do not remove validation without justification.
- Do not commit secrets.
- Do not weaken authz just to make a flow easier.
- Do not treat legacy fields as current product truth without explicit justification.

## 13. Standard Prompt Template for Future Tasks
Use this template for future backend tasks:

```md
You are working in the Mentor X backend repository: `mentorx-be`.

Before making changes:
1. Read `AGENTS.md`.
2. Read:
   - `/docs/rules/README.md`
   - `/docs/rules/00-product-foundation.md`
   - `/docs/rules/01-account-role-model.md`
   - `/docs/rules/02-business-flow.md`
   - `/docs/rules/13-ai-working-rules.md`
3. Also read the feature-specific rules for this task:
   - [LIST THE RELEVANT RULE FILES HERE]

Then:
1. Inspect the current implementation in the related source files.
2. Summarize the current behavior.
3. Summarize the rules that apply.
4. Propose the smallest safe change.
5. Modify only related files.
6. Run:
   `mvnw -q -DskipTests compile`
7. Report:
   - changed files
   - business logic impact
   - security impact
   - remaining risks or TODOs

Important constraints:
- Do not refactor unrelated modules.
- Do not add fake data.
- Do not trust caller-supplied `userId` for sensitive operations.
- Do not move business logic into mappers.
- Do not change wallet, escrow, contract, or auth flow semantics silently.
```
