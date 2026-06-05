# 06 Negotiation MVP

## Purpose
Freeze the negotiation model for the Mentor X MVP so backend logic stays aligned with the product decision.

## Core Rules
- MVP negotiation offer fields are only:
  - `price`
  - `deadlineAt`
  - `message`
- `message` is the main work-details field.
- `deadlineAt` is the agreed due date and time for the offer.
- `deadlineAt` is not a delivery-days shortcut and not an estimated-delivery surrogate field.
- Time remaining is a frontend display concern derived from `deadlineAt`.

## Explicitly Excluded From Default MVP Negotiation
- delivery days
- estimated delivery
- sessions
- separate scope field
- separate deliverables field
- hourly-rate-first negotiation

## Meaning Of Fields
- `price`
  - final or countered monetary offer in MXC
- `deadlineAt`
  - precise date-time expectation
  - backend validates that it is present and in the future when required
- `message`
  - work details
  - expectations
  - assumptions
  - included work context

## Product Semantics
- Accepting offer terms means both sides agree on `price + deadlineAt + message`.
- It does not mean the mentor is hired yet.
- It does not create the contract.
- It does not lock escrow.

## Presentation Alignment Rule
- Backend payloads and backend-owned examples must reinforce the MVP three-field negotiation model.
- Legacy fields must not be reintroduced as the default negotiation contract between frontend and backend.

## Backward Compatibility Rules
- Legacy fields may still exist in entities, DTOs, or database columns.
- Existing old fields must be treated as compatibility baggage, not active MVP design.
- New business logic must not require:
  - `estimatedDurationDays`
  - `proposedDeliveryDate`
  - `scopeDescription`
  - `deliverables`
  - `sessions`
- If old fields are still stored for compatibility, they must not drive new API requirements.

## API Rules
- Backend returns raw business fields and status codes.
- Backend does not return translated UI labels.
- Backend does not calculate display-only "time left" strings.

## Do
- Keep negotiation validation centered on amount, deadline, and message.
- Use `deadlineAt` consistently across proposal and negotiation responses.
- Document any temporary compatibility mapping clearly in service code.

## Do Not
- Reintroduce delivery days as the primary negotiation term.
- Split `message` into required `scope` and `deliverables` for default MVP flows.
- Make the negotiation service depend on session scheduling concepts.

## MVP Scope
- Clean counter-offer flow for mentor and client using three terms only.

## Future Scope / TODO
- If richer contract structure is needed later, add it as a post-MVP extension instead of silently expanding the default negotiation form.
