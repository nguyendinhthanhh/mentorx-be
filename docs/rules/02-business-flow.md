# 02 Business Flow

## Purpose
Describe Mentor X business flows in unambiguous language so service logic matches product decisions.

## Core Rules
- Business flow must remain linear and explicit.
- Offer agreement is not hiring.
- Hiring is not contract completion.
- Contract creation is the moment escrow is locked.

## User Flow
1. Register and activate account.
2. Complete onboarding if required.
3. Browse mentors, jobs, and courses.
4. Create a job or request.
5. Receive mentor proposals.
6. Negotiate terms if needed.
7. Accept a mentor.
8. Contract is created and escrow is locked.
9. Monitor work, chat, and review progress.
10. Mark complete or raise dispute.
11. Submit review after eligible completion.

## Mentor Flow
1. Register as a normal user.
2. Submit mentor profile and verification data.
3. Wait for approval.
4. After approval, access mentor workspace.
5. Publish mentor profile, offerings, or courses.
6. Find suitable jobs and submit proposals.
7. Negotiate terms.
8. Wait for client to accept the mentor.
9. Work inside active contract.
10. Receive escrow release on successful completion.
11. Request withdrawal through approved payout flow.

## Admin / Moderator Flow
- Moderator
  - review mentor applications
  - review reports and disputes
  - moderate abuse and content
- Admin
  - all moderator capabilities
  - withdrawal approval and rejection
  - refund and financial control decisions
  - platform settings and roles

## Job -> Proposal -> Negotiation -> Contract Lifecycle
1. Client creates job.
2. Mentor submits proposal.
3. Either side may negotiate terms.
4. Offer terms can become agreed.
5. Client accepts the mentor.
6. Backend creates contract.
7. Backend locks escrow.
8. Contract becomes active.
9. Client completes work or opens dispute or requests cancellation.

## Rules For Ambiguous States
- `offer terms agreed` means pricing and work details are aligned.
- `mentor accepted by client` means the client has selected that mentor for the job.
- `contract active` means escrow is already locked and work is live.
- `dispute` means escrow remains locked until resolved.

## Do
- Keep status transitions explicit in service logic.
- Align status names with real business milestones.
- Reject impossible transitions.

## Do Not
- Skip directly from negotiation to payout.
- Treat offer acceptance as contract creation.
- Reopen or mutate financial state without an explicit backend rule.

## MVP Scope
- Clear flow from job posting to escrow release.

## Future Scope / TODO
- Add a dedicated status transition table if lifecycle complexity grows further.
