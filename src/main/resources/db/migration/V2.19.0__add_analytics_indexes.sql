-- Phase 1.2: Add (target_type, target_id) index on view_events
-- Required because AnalyticsServiceImpl.getViewCount uses countByTargetTypeAndTargetId
-- which is a sequential scan on a growing table.

CREATE INDEX IF NOT EXISTS idx_view_events_target
    ON view_events (target_type, target_id);
