# 11 No Fake Data

## Purpose
Stop fake, demo, placeholder, or mock data from leaking into production-facing Mentor X backend behavior.

## Core Rules
- No fake or demo data may be exposed through production-facing APIs.
- Mock balances, mock activity, mock onboarding progress, and fake recommendations are not acceptable in live product flows.
- Demo routes and test helpers must not be publicly exposed in production runtime paths.

## Production API Rules
- If the backend cannot provide a real value yet, return an honest incomplete response or a feature-not-ready error.
- Do not fabricate:
  - wallet balances
  - escrow amounts
  - activity history
  - notification counts
  - mentor recommendations
  - knowledge feed data

## Allowed Dev/Test Data
- Local seed data is allowed only for development, testing, and controlled demos.
- Seed data must be clearly separated from production initialization.
- Runtime flags controlling sample data must be explicit and safe by default.

## Demo Route Rules
- Demo controllers, mock endpoints, and placeholder feed paths must not be enabled as normal production API surfaces.
- If a development-only feature exists, guard it with environment restrictions and obvious naming.

## Existing Legacy Data Rule
- Old sample scripts may stay in the repo temporarily.
- They must not silently initialize real runtime environments unless explicitly intended.

## Do
- Mark incomplete features honestly.
- Keep dev seed flows isolated from production startup.
- Remove or lock down production-visible placeholders quickly.

## Do Not
- Return fake numbers because a UI expects them.
- Use sample data to simulate completed business flow in production APIs.
- Expose placeholder recommendation endpoints as if they are real.

## MVP Scope
- Real business APIs only, even if some features remain incomplete.

## Future Scope / TODO
- Add environment-specific guards for any remaining sample-data bootstrap paths.
