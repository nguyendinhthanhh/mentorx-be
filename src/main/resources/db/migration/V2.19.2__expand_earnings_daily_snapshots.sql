-- Phase 2.1: Expand earnings_daily_snapshots with balance snapshots, source attribution, and engagement counts.
-- All columns are additive and default to 0 — safe for existing rows.

ALTER TABLE earnings_daily_snapshots
    ADD COLUMN IF NOT EXISTS escrow_balance_mxc           NUMERIC(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS available_balance_mxc        NUMERIC(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS earned_from_mentoring_mxc    NUMERIC(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS earned_from_freelance_mxc    NUMERIC(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS earned_from_courses_mxc      NUMERIC(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS proposals_sent               INTEGER       NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS proposals_accepted           INTEGER       NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS contracts_active             INTEGER       NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS contracts_completed          INTEGER       NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS course_enrollments           INTEGER       NOT NULL DEFAULT 0;

-- Index used by the analytics aggregation job: scans by snapshot_date across all users.
CREATE INDEX IF NOT EXISTS idx_earnings_daily_snapshots_date
    ON earnings_daily_snapshots (snapshot_date DESC);
