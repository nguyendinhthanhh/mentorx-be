-- PostgreSQL partial unique index:
-- keep full contract history, but guarantee only one live contract per job
-- when the contract is actively running or blocked in dispute.
CREATE UNIQUE INDEX IF NOT EXISTS ux_contracts_one_live_contract_per_job
    ON contracts (job_id)
    WHERE status IN ('ACTIVE', 'IN_DISPUTE');
