-- Add up migration script here

CREATE TABLE IF NOT EXISTS companies
(
    id       UUID         DEFAULT uuidv7() PRIMARY KEY,
    name     VARCHAR(120) NOT NULL,
    domain   VARCHAR(30)  NOT NULL UNIQUE,
    avatar   VARCHAR
);

CREATE INDEX IF NOT EXISTS companies_name_idx ON companies (domain);
