# 00 Product Foundation

## Purpose
Define what Mentor X backend is building so backend code follows the product, not generic marketplace patterns.

## Core Rules
- Mentor X is a mentor marketplace platform.
- The public marketplace is for browsing mentors, jobs, and courses.
- MentorHub is a mentor workspace, not a second marketplace.
- The backend must preserve the distinction between browsing, negotiation, selection, contract, and fulfillment.
- Legacy fields may exist in schema or DTOs, but they do not automatically define product truth.

## What Mentor X Is
- A platform where users can request mentor help through jobs or requests.
- A platform where approved mentors can submit proposals and negotiate terms.
- A platform where the client selects a mentor, which creates the contract and locks escrow.
- A platform with wallet, course, chat, review, moderation, and mentor verification support.

## What Mentor X Is Not
- Not a generic freelancer marketplace with broad deliverable-heavy contracting by default.
- Not a session-first coaching scheduler as the core negotiation model.
- Not a duplicate mentor-only marketplace inside MentorHub.
- Not a dashboard product that exposes fake balances, fake activity, or fake recommendations through production APIs.

## MVP Modules
- Account registration and login
- USER-first identity and RBAC
- Mentor application and approval
- Job creation and browsing
- Proposal submission
- Negotiation with `price + deadlineAt + message`
- Client accepts mentor
- Contract creation
- Escrow lock on contract creation
- Contract completion, dispute, cancellation
- Wallet deposit, release, refund, withdrawal approval
- Chat and notifications
- Reviews
- Admin and moderator management

## Out Of Scope For Now
- Session-based default negotiation contracts
- Delivery-days-first negotiation
- Separate default scope and deliverables fields in the negotiation MVP
- Hidden balance correction APIs for frontend use
- Production-facing demo controllers or mock financial data

## Do
- Use business language that matches the mentor marketplace model.
- Keep contract and escrow rules explicit and narrow.
- Treat mentor approval as a product gate, not only a UI flag.

## Do Not
- Reframe the product as a generic freelance board.
- Add new default negotiation concepts without product approval.
- Expose experimental or placeholder APIs as if they are product-complete.

## MVP Scope
- The MVP backend must support safe mentor selection and paid work completion with escrow.

## Future Scope / TODO
- If future versions support structured deliverables, milestones, or session packages by default, add them as explicit product decisions first.
