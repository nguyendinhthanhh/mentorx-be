-- MentorX bootstrap SQL (safe + idempotent)
-- Full production schema can be applied separately before running the app.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
