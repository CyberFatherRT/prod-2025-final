-- Add up migration script here

CREATE TABLE IF NOT EXISTS coworking_spaces
(
    id         UUID DEFAULT uuidv7() PRIMARY KEY,
    company_id UUID NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS items
(
    id          UUID DEFAULT uuidv7() PRIMARY KEY,
    name        VARCHAR,
    description VARCHAR,
    icon        VARCHAR,
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

