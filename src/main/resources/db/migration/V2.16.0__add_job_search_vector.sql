ALTER TABLE jobs
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (to_tsvector('simple', COALESCE(title, '') || ' ' || COALESCE(description, ''))) STORED;

CREATE INDEX idx_jobs_search_vector ON jobs USING gin(search_vector);
