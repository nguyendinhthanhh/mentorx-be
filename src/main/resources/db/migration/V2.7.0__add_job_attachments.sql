-- Add attachment fields to jobs table
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS attachment_url VARCHAR(500);

-- Create job_attachments_list table for multiple attachments
CREATE TABLE IF NOT EXISTS job_attachments_list (
    job_id UUID NOT NULL,
    attachment_url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_job_attachments_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_job_attachments_list_job_id ON job_attachments_list(job_id);

-- Add comment
COMMENT ON TABLE job_attachments_list IS 'Stores multiple attachment URLs for job postings';
COMMENT ON COLUMN jobs.attachment_url IS 'Primary attachment URL for backward compatibility';
