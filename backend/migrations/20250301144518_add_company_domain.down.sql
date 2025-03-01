-- Add down migration script here
ALTER TABLE users
    DROP COLUMN company_domain;