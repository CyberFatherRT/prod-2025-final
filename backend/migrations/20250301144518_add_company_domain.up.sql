-- Add up migration script here
ALTER TABLE users
    ADD COLUMN company_domain VARCHAR NOT NULL DEFAULT NULL;
