-- Add up migration script here

CREATE TABLE IF NOT EXISTS pending_verifications
(
    user_id       UUID    PRIMARY KEY,
    company_id    UUID    NOT NULL,
    document_name VARCHAR NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE INDEX pending_verifications_company_id_idx ON pending_verifications (company_id);

