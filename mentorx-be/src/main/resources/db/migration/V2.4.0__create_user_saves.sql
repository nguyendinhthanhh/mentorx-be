CREATE TABLE IF NOT EXISTS user_saves (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(30) NOT NULL,
    target_id   UUID NOT NULL,
    saved_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_saves_target UNIQUE (user_id, target_type, target_id)
);

CREATE INDEX IF NOT EXISTS idx_user_saves_user_type
    ON user_saves(user_id, target_type, saved_at DESC);

CREATE INDEX IF NOT EXISTS idx_user_saves_target
    ON user_saves(target_type, target_id);
