-- Add down migration script here
ALTER TABLE companies
    DROP COLUMN domain;
ALTER TABLE users
    DROP CONSTRAINT fk_company_domain_users;