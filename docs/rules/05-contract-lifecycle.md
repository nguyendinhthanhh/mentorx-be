# 05 Contract Lifecycle

## Purpose
Lock the contract lifecycle to the confirmed Mentor X product flow.

## Core Rules
- Accepting offer terms does not create a contract.
- Accepting offer terms does not lock escrow.
- Contract is created only when the client accepts the mentor.
- Contract creation is the event that locks escrow.
- Contract completion releases escrow to the mentor.

## Exact Lifecycle
1. Proposal exists.
2. Negotiation may happen.
3. Offer terms become agreed.
4. Client accepts the mentor.
5. Backend creates the contract.
6. Backend locks escrow.
7. Contract becomes active.
8. Work proceeds.
9. Completion, dispute, or cancellation flow happens.

## Status Meaning
- `terms agreed`
  - both sides accepted negotiation terms
  - no contract yet
  - no escrow lock yet
- `contract created`
  - selected mentor is now hired for the job
  - escrow is locked as part of the same flow
- `active`
  - contract is live and funded
- `completed`
  - escrow released to mentor
- `in dispute`
  - escrow remains locked
- `cancelled`
  - apply explicit refund or resolution rules

## Contract Creation Rules
- Only the job owner or allowed admin path can trigger mentor acceptance.
- Contract creation must verify job eligibility, proposal eligibility, mentor approval, and funding ability.
- Contract creation must not happen from a negotiation accept endpoint.

## Completion Rules
- Only the correct contract participant can complete through the defined flow.
- Completion must verify active status and escrow presence.
- Completion must release funds exactly once.

## Dispute Rules
- Dispute blocks normal release.
- Dispute resolution must explicitly define whether escrow is released, partially refunded, or fully refunded.

## Cancellation Rules
- Cancellation behavior must state:
  - who can request it
  - who can approve it
  - what happens to escrow
  - whether the job reopens

## Do
- Keep contract transitions narrow and auditable.
- Validate participant rights for every contract mutation.
- Align job status updates with contract state.

## Do Not
- Allow sign or activate flows to bypass participant checks.
- Create direct draft contracts from public business flows unless explicitly justified.
- Conflate proposal acceptance, negotiation acceptance, and mentor acceptance.

## MVP Scope
- Single clear path from agreed terms to funded active contract.

## Future Scope / TODO
- If milestones become primary funding units, define milestone contract lifecycle separately without changing this base rule silently.
