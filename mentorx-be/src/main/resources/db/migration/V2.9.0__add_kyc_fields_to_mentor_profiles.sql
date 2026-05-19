ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_document_back_url TEXT;
ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS verification_metadata JSONB;
ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS rejection_reason TEXT;
ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS approved_by UUID REFERENCES users(id);
ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ;

-- Ensure identity_document_type exists as requested
ALTER TABLE mentor_profiles ADD COLUMN IF NOT EXISTS identity_document_type VARCHAR(50);
