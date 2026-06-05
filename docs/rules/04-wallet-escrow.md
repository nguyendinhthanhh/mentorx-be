# 04 Wallet Escrow

## Purpose
Define financially safe wallet, escrow, refund, release, and withdrawal behavior for Mentor X.

## Core Rules
- MXC is the platform wallet currency used for internal settlement.
- No direct balance edits from frontend or normal business APIs.
- Every balance change must come from a validated backend transaction flow.
- Escrow lock, release, and refund must be idempotent.
- Disputes keep escrow locked until an explicit resolution flow completes.

## Wallet Principles
- User-facing balances may include separate concepts such as available and pending when needed.
- System wallets may exist for escrow, revenue, or platform float, but they must be internal-only concerns.
- Balance calculations must not rely on UI assumptions.
- Ledger state and wallet state must not drift silently.

## Escrow Principles
- Escrow is locked only when a contract is created.
- Escrow is released only when completion is confirmed by the correct backend flow.
- Escrow is refunded only by a valid cancellation or refund path.
- Dispute means funds remain locked until resolution.

## Source Of Truth Rules
- Choose one canonical meaning for each of these:
  - wallet ledger entries
  - wallet balance snapshot
  - contract escrow flags
  - escrow records
- If multiple representations exist, service logic must keep them consistent within the same transaction.
- Do not add new escrow state fields without defining the canonical owner of truth.

## Idempotency Rules
- Payment lock must not double-charge on repeated requests.
- Escrow release must not pay the mentor twice.
- Refund must not return the same escrow amount twice.
- Gateway callbacks and manual retries must be safe to repeat.

## Withdrawal Rules
- Withdrawal is a request, not an immediate payout.
- Only eligible mentors can request withdrawals.
- Only admins can approve or reject withdrawals.
- Moderators cannot approve withdrawals.

## No Direct Balance Edits
- No public admin endpoint should arbitrarily set balance values.
- No frontend should submit final ledger balances.
- Any exceptional financial correction must be implemented as an auditable backend financial operation, not a casual edit.

## Do
- Validate available balance before lock or withdrawal.
- Record every financial mutation with enough audit context.
- Keep refund and release paths mutually exclusive.

## Do Not
- Mix display fields with settlement truth.
- Permit client code to decide final escrow state.
- Release funds while dispute is open.

## MVP Scope
- Safe contract-funded escrow with release, refund, and dispute hold behavior.

## Future Scope / TODO
- Document platform fee treatment and reconciliation in more detail if fee logic expands.
