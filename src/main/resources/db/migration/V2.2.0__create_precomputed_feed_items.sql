-- ============================================================
-- MIGRATION: V2.2.0 - Precomputed Feed Items
-- Description: Create table for storing precomputed personalized feed items
--              Part of Personalized Discovery Dashboard feature
-- Author: MentorX Development Team
-- Date: 2026-05-07
-- ============================================================

-- Create enum for feed item types
DO $$ BEGIN
    CREATE TYPE feed_item_type AS ENUM (
        'MENTOR',
        'COURSE',
        'KNOWLEDGE',
        'JOB'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create precomputed_feed_items table
CREATE TABLE IF NOT EXISTS precomputed_feed_items (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_type     feed_item_type NOT NULL,
    item_id       UUID NOT NULL,
    match_score   NUMERIC(5, 2) NOT NULL,
    computed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at    TIMESTAMPTZ NOT NULL,
    metadata      JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_match_score_range CHECK (match_score >= 0.00 AND match_score <= 100.00),
    CONSTRAINT chk_expires_after_computed CHECK (expires_at > computed_at)
);

-- Create indexes for query performance
CREATE INDEX idx_precomputed_feed_user_id 
    ON precomputed_feed_items(user_id);

CREATE INDEX idx_precomputed_feed_user_computed 
    ON precomputed_feed_items(user_id, computed_at DESC);

CREATE INDEX idx_precomputed_feed_user_expires 
    ON precomputed_feed_items(user_id, expires_at);

CREATE INDEX idx_precomputed_feed_user_type_score 
    ON precomputed_feed_items(user_id, item_type, match_score DESC);

CREATE INDEX idx_precomputed_feed_expires 
    ON precomputed_feed_items(expires_at);

-- Create composite index for efficient feed retrieval
CREATE INDEX idx_precomputed_feed_active_items 
    ON precomputed_feed_items(user_id, item_type, match_score DESC) 
    WHERE expires_at > NOW();

-- Add trigger for auto-updating updated_at
CREATE TRIGGER trg_precomputed_feed_items_updated_at
    BEFORE UPDATE ON precomputed_feed_items 
    FOR EACH ROW 
    EXECUTE FUNCTION fn_set_updated_at();

-- Add comment for documentation
COMMENT ON TABLE precomputed_feed_items IS 
    'Stores precomputed personalized feed items for users. Items are calculated by the matching engine and cached for 24 hours.';

COMMENT ON COLUMN precomputed_feed_items.match_score IS 
    'Match score percentage (0-100) calculated using formula: (skillMatch * 0.6) + (levelMatch * 0.3) + (ratingBonus * 0.1)';

COMMENT ON COLUMN precomputed_feed_items.expires_at IS 
    'Expiration timestamp, typically computed_at + 24 hours. Expired items should be filtered out.';

COMMENT ON COLUMN precomputed_feed_items.metadata IS 
    'Additional metadata about the recommendation (e.g., matching skills, level match details)';
