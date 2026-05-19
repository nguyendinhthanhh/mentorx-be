-- Create proposal_negotiations table for tracking negotiation history
CREATE TABLE IF NOT EXISTS proposal_negotiations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id UUID NOT NULL REFERENCES proposals(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id),
    sender_type VARCHAR(20) NOT NULL CHECK (sender_type IN ('CLIENT', 'MENTOR')),
    
    -- Negotiation details
    message TEXT NOT NULL,
    proposed_amount DECIMAL(10, 2),
    proposed_hourly_rate DECIMAL(8, 2),
    estimated_duration_days SMALLINT,
    proposed_start_date DATE,
    proposed_delivery_date DATE,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COUNTERED')),
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    
    -- Indexes
    CONSTRAINT fk_negotiation_proposal FOREIGN KEY (proposal_id) REFERENCES proposals(id) ON DELETE CASCADE,
    CONSTRAINT fk_negotiation_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_negotiation_proposal_id ON proposal_negotiations(proposal_id);
CREATE INDEX idx_negotiation_sender_id ON proposal_negotiations(sender_id);
CREATE INDEX idx_negotiation_status ON proposal_negotiations(status);
CREATE INDEX idx_negotiation_created ON proposal_negotiations(created_at DESC);

-- Add comment
COMMENT ON TABLE proposal_negotiations IS 'Stores negotiation history between clients and mentors for proposals';
COMMENT ON COLUMN proposal_negotiations.sender_type IS 'Who initiated this negotiation round: CLIENT or MENTOR';
COMMENT ON COLUMN proposal_negotiations.status IS 'Status of this negotiation: PENDING (waiting response), ACCEPTED (agreed), REJECTED (declined), COUNTERED (new counter-offer made)';
