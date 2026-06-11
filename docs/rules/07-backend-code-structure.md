# 07 Backend Code Structure

## Purpose
Keep the Mentor X backend modular, predictable, and resistant to messy AI-generated changes.

## Core Rules
- Follow the existing modular monolith structure.
- Controllers are thin.
- Services contain business logic.
- Repositories only access persistence.
- DTOs shield entities from direct API exposure.

## Standard Structure
- `controller`
- `service`
- `service.impl`
- `repository`
- `entity`
- `dto.request`
- `dto.response`
- `mapper`
- `exception`
- `validation` when needed

## Controller Rules
- Parse request input.
- Trigger service methods.
- Return standardized responses.
- Keep controller logic small and readable.
- Do not place authorization-critical business rules only in controllers.

## Service Rules
- Own all business decisions and status transitions.
- Validate ownership, role, and lifecycle constraints.
- Coordinate repository access and cross-module orchestration.
- Handle transaction boundaries where financial or lifecycle consistency matters.

## Repository Rules
- Read and write persistence only.
- No business orchestration.
- No permission checks.
- No DTO composition that hides business meaning.

## DTO Rules
- Use request DTOs for incoming API payloads.
- Use response DTOs for outgoing payloads.
- Do not expose JPA entities directly through controllers.
- Do not accept sensitive state from frontend unless it is explicitly admin-only and justified.

## Sensitive Field Rules
- Frontend must not set final financial balances.
- Frontend must not choose the acting user for sensitive operations.
- Frontend must not set authoritative contract or escrow state flags directly.

## Do
- Add logic to the existing module that owns the domain concept.
- Prefer small service methods with explicit naming.
- Keep authz close to the business action.

## Do Not
- Put business logic in mappers or repositories.
- Create "god service" shortcuts across unrelated modules.
- Return entity internals just because they are easy to serialize.

## MVP Scope
- Preserve maintainable module boundaries while stabilizing product flows.

## Future Scope / TODO
- Consolidate duplicate patterns only after business rules are stable.
