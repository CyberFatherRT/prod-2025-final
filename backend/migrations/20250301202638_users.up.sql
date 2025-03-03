-- Add up migration script here

DO $$
BEGIN
    CREATE TYPE ROLE AS ENUM ('admin', 'student', 'guest', 'verified_guest');
EXCEPTION
    WHEN DUPLICATE_OBJECT THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS users
(
    id             UUID                  DEFAULT uuidv7() PRIMARY KEY,
    name           VARCHAR(120) NOT NULL,
    surname        VARCHAR(120) NOT NULL,
    email          VARCHAR(120) NOT NULL,
    password       VARCHAR(120) NOT NULL,
    avatar         VARCHAR,
    company_id     UUID         NOT NULL,
    company_domain VARCHAR(30)  NOT NULL,
    role           ROLE         NOT NULL DEFAULT 'guest',
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    UNIQUE (email, company_id)
);

CREATE TABLE IF NOT EXISTS pending_verifications
(
    user_id    UUID PRIMARY KEY,
    company_id UUID,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS pending_verifications_company_id_idx ON pending_verifications (company_id);
