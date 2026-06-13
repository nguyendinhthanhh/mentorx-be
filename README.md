# Mentor X Backend API

Mentor X Backend API is a Spring Boot modular monolith for a mentor marketplace that goes well beyond simple CRUD.  
It supports mentor discovery, job and proposal workflows, negotiation, contract creation, escrow-aware wallet operations, messaging, reviews, verification, and moderation in a single cohesive backend.

This repository is built to reflect real product rules and operational constraints, not a generic freelance-platform sample.

## Executive Summary

Mentor X models the full path from discovery to paid collaboration:

- users browse mentors, jobs, and courses
- users post support requests
- approved mentors submit proposals and negotiate terms
- client selection creates the contract
- escrow is locked when the contract is created
- completion, cancellation, dispute, release, and refund flows are handled explicitly
- admins and moderators operate verification, moderation, and financial controls

## Why This Backend Stands Out

- It contains real business workflows with lifecycle rules, not just resource endpoints
- It enforces role and ownership constraints across sensitive product areas
- It handles money-adjacent logic through wallet, deposit, escrow, and withdrawal domains
- It integrates external systems for auth, mail, payments, media, and verification
- It keeps a layered architecture inside a single deployable backend for practical product delivery

## Core Product Domains

- Authentication and account lifecycle
- USER to MENTOR approval flow
- Mentor public profile and marketplace presence
- Job posting, proposal submission, and negotiation
- Contract creation and lifecycle transitions
- Wallet, deposit, escrow, release, refund, and withdrawal handling
- Chat and notifications
- Reviews and trust signals
- Admin moderation and mentor verification
- File storage for public assets and private verification documents

## Architectural Approach

The project uses a modular monolith structure with strong layer separation:

```text
src/main/java/com/mentorx/api/
  auth/
  common/
  feature/
    chat/
    course/
    feed/
    job/
    mentor/
    payment/
    review/
    system/
    user/
    wallet/
```

Key implementation boundaries:

- `controller` handles HTTP input and output only
- `service` and `service.impl` own business orchestration
- `repository` owns persistence access
- `dto.request` and `dto.response` define API contracts
- `mapper` is restricted to shape transformation, not business policy

This separation matters because the product includes financial, authorization, and lifecycle-sensitive rules that should not leak into controllers or mapping code.

## Engineering Highlights

- JWT-based authentication plus OAuth2 client integration
- Role-aware and ownership-aware authorization patterns
- Explicit contract and escrow lifecycle modeling
- Wallet transaction flows with auditable financial state changes
- Public vs private file routing for media and verification flows
- Support for external payment providers and callback handling
- OCR and image-processing support for verification-oriented workflows
- OpenAPI documentation for API exploration and testing

## Technology Stack

- `Java 21`
- `Spring Boot 3.2.5`
- `Spring Security`
- `Spring Data JPA`
- `PostgreSQL`
- `Redis`
- `WebSocket`
- `Spring Mail`
- `MapStruct`
- `JWT`
- `OpenAPI / Swagger`
- `Docker`
- `Testcontainers`

Supporting libraries in the repository also cover OCR, PDF processing, and image handling for identity and document-related workflows.

## What a Reviewer Should Notice

This codebase is most valuable as a hiring signal in these areas:

- backend design for multi-role product systems
- handling of negotiation, contract, and escrow rules in one domain model
- discipline around service-layer ownership of business logic
- practical integration of external systems without collapsing architecture
- safety-minded handling of financial and verification flows

## Representative Business Rules

- every account begins as `USER`
- mentor access is an approved capability, not a separate account type
- proposal negotiation is centered on `price + deadlineAt + message`
- contract creation is the point that locks escrow
- contract completion is the point that releases escrow
- dispute keeps escrow locked until resolution
- financial controls are separated from general moderation responsibilities

These rules make the backend meaningfully different from a generic CRUD board.

## Local Development

### Prerequisites

- `Java 21`
- `Maven` or the included Maven Wrapper
- `PostgreSQL`
- `Redis`
- `Docker` and `docker-compose` for local infrastructure if preferred

### Environment Setup

Create `.env` from `.env.example`:

```bash
cp .env.example .env
```

Typical configuration areas:

- database connection
- Redis connection
- JWT secret and token settings
- wallet secret key
- OAuth credentials
- SMTP configuration
- payment provider credentials
- Cloudinary credentials
- storage provider settings

Do not commit real secrets.

### Start Supporting Services

```bash
docker-compose up -d postgres redis
```

### Start the Application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

Default local API base:

- `http://localhost:8080`

## Build

Windows:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

macOS / Linux:

```bash
./mvnw -q -DskipTests compile
```

## Test

```bash
./mvnw test
```

The project also includes `Testcontainers` dependencies for integration-oriented testing.

## API Documentation

When the application is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI docs: `http://localhost:8080/api-docs`

## Operational Notes

- public image assets can be routed to Cloudinary
- private verification and sensitive document flows can remain on controlled storage
- payment integrations are exposed through backend-controlled endpoints rather than pushing money logic into the client
- wallet and escrow flows should remain explicit, auditable, and lifecycle-driven

## Recruiter Notes

If you are reviewing this project as engineering signal, the strongest backend themes are:

- non-trivial backend domain modeling
- product-grade authorization and ownership checks
- financial and lifecycle-aware service design
- modular monolith architecture with clear boundaries
- integration-heavy backend work without turning the codebase into a fragile script collection

## Related Repository

- Frontend client: [mentorx-fe](https://github.com/nguyendinhthanhh/mentorx-fe)
