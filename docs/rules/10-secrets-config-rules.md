# 10 Secrets Config Rules

## Purpose
Prevent unsafe secrets, payment defaults, and auth defaults in Mentor X backend configuration.

## Core Rules
- Do not commit real secrets.
- Do not keep unsafe payment, JWT, OAuth, wallet, or mail defaults in committed config.
- Environment variables are the preferred source for secrets.
- Local defaults must be safe for development and obviously non-production.

## Secret Categories
- JWT signing secrets
- wallet or ledger signing secrets
- OAuth client secrets
- payment gateway secrets
- mail credentials
- database passwords

## Safe Local Default Rules
- Local defaults must be clearly fake and unusable outside local development.
- Never commit production-like gateway secrets, client IDs paired with live secrets, or reusable auth defaults.
- If a local default is required, it must force developers to replace it in real environments.

## Payment Config Rules
- Payment gateway config must be environment-driven.
- Callback URLs must be environment-specific.
- Sandbox credentials must still be treated carefully and should not be used as hidden permanent defaults.

## Auth Config Rules
- JWT secret must be strong and environment-provided.
- OAuth secrets must never be committed.
- Mail passwords or app passwords must never be committed.

## Config Hygiene
- `.env` may exist locally, but must not expose real secrets in source control.
- `.env.example` should describe required variables without revealing sensitive values.
- Production profiles must not inherit dangerous local fallbacks silently.

## Do
- Use environment variables for secrets.
- Rotate exposed secrets if they were ever committed.
- Review config files for accidental financial or auth defaults.

## Do Not
- Commit reusable gateway secrets.
- Commit a weak default JWT secret.
- Treat "dev only" secrets as harmless when the repo is shared.

## MVP Scope
- Safe local development config and secure deployable config boundaries.

## Future Scope / TODO
- Add a pre-commit or CI secret scan policy if the repo workflow allows it.
