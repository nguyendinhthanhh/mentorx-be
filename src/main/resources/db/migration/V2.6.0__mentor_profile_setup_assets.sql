ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS video_intro_url TEXT;
ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS location VARCHAR(150);
ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS languages JSONB;

CREATE TABLE IF NOT EXISTS mentor_profile_assets (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mentor_profile_id UUID NOT NULL REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    asset_type        VARCHAR(30) NOT NULL,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    issuer            VARCHAR(150),
    file_url          TEXT,
    icon_url          TEXT,
    issued_at         DATE,
    is_featured       BOOLEAN NOT NULL DEFAULT FALSE,
    display_order     INTEGER DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mentor_profile_assets_profile
    ON mentor_profile_assets(mentor_profile_id, asset_type, display_order);

CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_mentor_profile_assets_updated_at ON mentor_profile_assets;
CREATE TRIGGER trg_mentor_profile_assets_updated_at
    BEFORE UPDATE ON mentor_profile_assets FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
