CREATE TABLE email_verification_tokens (
    id                       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id                  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token                    VARCHAR(255) NOT NULL UNIQUE,
    email                    VARCHAR(255) NOT NULL,
    expires_at               TIMESTAMPTZ NOT NULL,
    is_used                  BOOLEAN NOT NULL DEFAULT FALSE,
    used_at                  TIMESTAMPTZ,
    request_ip               VARCHAR(45),
    verification_ip          VARCHAR(45),
    request_user_agent       VARCHAR(500),
    verification_user_agent  VARCHAR(500),
    attempt_count            INTEGER NOT NULL DEFAULT 0,
    last_attempt_at          TIMESTAMPTZ,
    is_resend                BOOLEAN NOT NULL DEFAULT FALSE,
    original_token_id        UUID,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at               TIMESTAMPTZ
);

CREATE INDEX idx_evt_user_id ON email_verification_tokens(user_id);
CREATE INDEX idx_evt_token ON email_verification_tokens(token);
CREATE INDEX idx_evt_email ON email_verification_tokens(email);
CREATE INDEX idx_evt_expires ON email_verification_tokens(expires_at);
CREATE INDEX idx_evt_used ON email_verification_tokens(is_used);
