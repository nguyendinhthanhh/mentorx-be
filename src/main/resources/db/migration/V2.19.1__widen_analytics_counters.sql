-- Phase 1.3: Widen jobs_completed and courses_sold from SMALLINT (max 32,767) to INTEGER
-- Prevents overflow for high-volume users. Backfill any existing NULLs to 0.

ALTER TABLE earnings_daily_snapshots
    ALTER COLUMN jobs_completed TYPE INTEGER USING jobs_completed::INTEGER,
    ALTER COLUMN courses_sold   TYPE INTEGER USING courses_sold::INTEGER;

UPDATE earnings_daily_snapshots
   SET jobs_completed = 0
 WHERE jobs_completed IS NULL;

UPDATE earnings_daily_snapshots
   SET courses_sold = 0
 WHERE courses_sold IS NULL;

ALTER TABLE earnings_daily_snapshots
    ALTER COLUMN jobs_completed SET NOT NULL,
    ALTER COLUMN jobs_completed SET DEFAULT 0,
    ALTER COLUMN courses_sold   SET NOT NULL,
    ALTER COLUMN courses_sold   SET DEFAULT 0;
