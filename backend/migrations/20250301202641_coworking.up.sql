-- Add up migration script here

DO $$
BEGIN
    CREATE TYPE point AS (
        x INTEGER,
        y INTEGER
    );
EXCEPTION
    WHEN DUPLICATE_OBJECT THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS coworking_spaces
(
    id         UUID DEFAULT uuidv7() PRIMARY KEY,
    height     INTEGER NOT NULL,
    width      INTEGER NOT NULL,
    company_id UUID    NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS items
(
    id          UUID    DEFAULT uuidv7() PRIMARY KEY,
    name        VARCHAR               NOT NULL,
    description VARCHAR,
    icon        VARCHAR,
    offsets     point[]               NOT NULL,
    bookable    BOOLEAN DEFAULT FALSE NOT NULL,
    company_id  UUID                  NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS coworking_items
(
    id           UUID DEFAULT uuidv7() PRIMARY KEY,
    item_id      UUID NOT NULL,
    base_point   point,
    coworking_id UUID NOT NULL,
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (coworking_id) REFERENCES coworking_spaces (id)
);

