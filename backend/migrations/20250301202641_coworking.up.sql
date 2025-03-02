-- Add up migration script here

CREATE TYPE point AS (
    x INTEGER,
    y INTEGER
);

CREATE TABLE IF NOT EXISTS coworking_spaces
(
    id         UUID    DEFAULT uuidv7() PRIMARY KEY,
    height     INTEGER NOT NULL,
    width      INTEGER NOT NULL,
    company_id UUID    NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS items
(
    id          UUID DEFAULT uuidv7() PRIMARY KEY,
    name        VARCHAR,
    description VARCHAR,
    icon        VARCHAR,
    base_point  point,
    offsets     point[],
    rotation    SMALLINT CHECK (rotation BETWEEN 0 AND 3),
    company_id  UUID NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS coworking_items
(
    id           UUID DEFAULT uuidv7() PRIMARY KEY,
    item_id      UUID NOT NULL,
    coworking_id UUID NOT NULL,
    FOREIGN KEY (item_id) REFERENCES items (id),
    FOREIGN KEY (coworking_id) REFERENCES coworking_spaces (id)
);

