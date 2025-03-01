-- Add down migration script here

DROP INDEX IF EXISTS companies_name_idx;
DROP TABLE IF EXISTS companies;
