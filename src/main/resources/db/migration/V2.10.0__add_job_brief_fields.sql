ALTER TABLE jobs ADD COLUMN IF NOT EXISTS experience_level VARCHAR(80);
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS current_level VARCHAR(120);
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS learning_goals TEXT;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS success_criteria TEXT;
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS availability_expectation VARCHAR(255);
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS communication_preference VARCHAR(120);

CREATE TABLE IF NOT EXISTS job_required_skills (
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill VARCHAR(120) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_job_required_skills_job_id
    ON job_required_skills(job_id);
