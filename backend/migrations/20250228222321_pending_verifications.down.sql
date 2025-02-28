-- Add down migration script here

DROP INDEX IF EXISTS pending_verifications_company_id_idx;
DROP TABLE IF EXISTS pending_verifications;
