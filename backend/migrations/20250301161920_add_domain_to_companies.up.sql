-- Add up migration script here
ALTER TABLE companies
    ADD COLUMN domain VARCHAR UNIQUE NOT NULL DEFAULT NULL;
ALTER TABLE users
    ADD CONSTRAINT fk_company_domain_users FOREIGN KEY (company_domain)
        REFERENCES companies (domain);
