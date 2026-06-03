ALTER TABLE proposals
    ADD COLUMN IF NOT EXISTS deadline_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS scope_description VARCHAR(1000);

ALTER TABLE proposal_negotiations
    ADD COLUMN IF NOT EXISTS deadline_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS scope_description VARCHAR(1000);

ALTER TABLE contracts
    ADD COLUMN IF NOT EXISTS deadline_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS scope_description VARCHAR(1000);

COMMENT ON COLUMN proposals.deadline_at IS 'Exact deadline proposed or agreed for the offer terms';
COMMENT ON COLUMN proposals.scope_description IS 'Scope and deliverables included in the proposal terms';
COMMENT ON COLUMN proposal_negotiations.deadline_at IS 'Exact deadline proposed for this negotiation offer';
COMMENT ON COLUMN proposal_negotiations.scope_description IS 'Scope and deliverables included in this negotiation offer';
COMMENT ON COLUMN contracts.deadline_at IS 'Exact deadline agreed for the active contract';
COMMENT ON COLUMN contracts.scope_description IS 'Scope and deliverables agreed for the active contract';
