# 09 Database Rules

## Purpose
Set database and migration rules for Mentor X backend on PostgreSQL.

## Core Rules
- PostgreSQL is the supported application database.
- Schema changes must be intentional and reviewable.
- Financial and contract-critical tables require strong constraints and indexes.
- Backward compatibility for existing data matters, but must not justify unsafe runtime schema hacks forever.

## Migration Rules
- Prefer explicit versioned migrations for schema changes.
- Avoid silently patching business schema at runtime unless it is a temporary compatibility bridge.
- Document temporary compatibility patches and plan their removal.

## Constraint Rules
- Add or preserve uniqueness where business identity requires it.
- Add foreign keys for contract, proposal, escrow, and wallet relationships.
- Add status and state constraints where invalid values create financial risk.
- Enforce single-active-contract assumptions at the database level where possible.

## Index Rules
- Index participant and lifecycle lookup paths for:
  - contracts
  - proposals
  - negotiations
  - wallet transactions
  - escrow records
  - withdrawals
- Add indexes for idempotency and gateway reconciliation identifiers.

## Unsafe Schema Rules
- Do not rely on ad hoc SQL patches as a permanent migration strategy.
- Do not keep multiple conflicting schema definitions without a clearly documented source of truth.
- Do not add columns that duplicate authoritative financial state unless the owner of truth is defined.

## Old Data Compatibility
- Legacy columns may stay temporarily for compatibility.
- New code must define which fields are legacy and which are active.
- Compatibility code must not expand product scope silently.

## Do
- Keep schema changes minimal and explicit.
- Protect financially important tables with constraints.
- Review runtime initializers that modify schema and move stable changes into migrations.

## Do Not
- Depend on loosely controlled boot-time schema mutation for core business evolution.
- Add nullable financial meaning fields without careful review.
- Treat old seed schema files as canonical if migrations disagree.

## MVP Scope
- Stable, constrained schema for accounts, proposals, contracts, escrow, and wallet flows.

## Future Scope / TODO
- Consolidate duplicate schema bootstrap paths into one clear migration strategy.
