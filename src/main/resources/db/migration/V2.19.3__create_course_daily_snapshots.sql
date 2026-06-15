-- Phase 4 (DEC-004 Option B): Per-course granularity lives in its own table.
-- Aggregation job upserts one row per (course, snapshot_date).

CREATE TABLE IF NOT EXISTS course_daily_snapshots (
    id                       UUID PRIMARY KEY,
    course_id                UUID NOT NULL REFERENCES courses(id),
    snapshot_date            DATE NOT NULL,
    enrollments_count        INTEGER NOT NULL DEFAULT 0,
    sold_count               INTEGER NOT NULL DEFAULT 0,
    revenue_mxc              NUMERIC(15,4) NOT NULL DEFAULT 0,
    completion_rate          NUMERIC(5,4)  NOT NULL DEFAULT 0,
    lesson_views             INTEGER       NOT NULL DEFAULT 0,
    average_rating           NUMERIC(3,2)  NOT NULL DEFAULT 0,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_course_daily UNIQUE (course_id, snapshot_date)
);

CREATE INDEX IF NOT EXISTS idx_course_daily_snapshots_date
    ON course_daily_snapshots (snapshot_date DESC);

CREATE INDEX IF NOT EXISTS idx_course_daily_snapshots_course
    ON course_daily_snapshots (course_id);
