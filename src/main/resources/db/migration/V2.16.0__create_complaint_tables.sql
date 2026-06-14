CREATE TABLE IF NOT EXISTS complaints (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    complainant_id          UUID NOT NULL,
    respondent_id           UUID NOT NULL,
    session_id              UUID,
    booking_id              UUID,
    title                   VARCHAR(200) NOT NULL,
    description             TEXT NOT NULL,
    complaint_category      VARCHAR(50) NOT NULL,
    status                  VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    priority_level          INTEGER NOT NULL DEFAULT 3,
    mediator_id             UUID,
    mediator_assigned_at    TIMESTAMPTZ,
    respondent_notified_at  TIMESTAMPTZ,
    respondent_responded_at TIMESTAMPTZ,
    respondent_response     TEXT,
    response_deadline       TIMESTAMPTZ,
    mediation_started_at    TIMESTAMPTZ,
    resolved_at             TIMESTAMPTZ,
    outcome                 VARCHAR(30),
    resolution_details      VARCHAR(2000),
    resolution_time_hours   DOUBLE PRECISION,
    sla_met                 BOOLEAN,
    sla_deadline            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_complaint_complainant_id ON complaints(complainant_id);
CREATE INDEX idx_complaint_respondent_id ON complaints(respondent_id);
CREATE INDEX idx_complaint_status ON complaints(status);
CREATE INDEX idx_complaint_booking_id ON complaints(booking_id);
CREATE INDEX idx_complaint_created ON complaints(created_at DESC);

CREATE TABLE IF NOT EXISTS complaint_evidence (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    complaint_id         UUID NOT NULL,
    submitted_by_user_id UUID NOT NULL,
    evidence_type        VARCHAR(30) NOT NULL,
    title                VARCHAR(200) NOT NULL,
    description          VARCHAR(1000),
    file_url             VARCHAR(500),
    filename             VARCHAR(255),
    mime_type            VARCHAR(100),
    file_size            BIGINT,
    is_reviewed          BOOLEAN NOT NULL DEFAULT FALSE,
    reviewed_at          TIMESTAMPTZ,
    reviewed_by_user_id  UUID,
    review_notes         VARCHAR(1000),
    is_flagged           BOOLEAN NOT NULL DEFAULT FALSE,
    flag_reason          VARCHAR(500),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_complaint_evidence_complaint_id ON complaint_evidence(complaint_id);
CREATE INDEX idx_complaint_evidence_submitted_by ON complaint_evidence(submitted_by_user_id);
CREATE INDEX idx_complaint_evidence_created ON complaint_evidence(created_at DESC);
