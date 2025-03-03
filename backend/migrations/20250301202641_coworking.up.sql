-- Add up migration script here

DO
$$
    BEGIN
        CREATE TYPE point AS
        (
            x INT,
            y INT
        );
    EXCEPTION
        WHEN DUPLICATE_OBJECT THEN NULL;
    END
$$;

CREATE TABLE IF NOT EXISTS buildings
(
    id         UUID DEFAULT uuidv7() PRIMARY KEY,
    address    VARCHAR NOT NULL,
    company_id UUID    NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS buildings_company_id_idx ON buildings (company_id);


CREATE TABLE IF NOT EXISTS coworking_spaces
(
    id          UUID DEFAULT uuidv7() PRIMARY KEY,
    address     VARCHAR NOT NULL,
    height      INT     NOT NULL,
    width       INT     NOT NULL,
    building_id UUID    NOT NULL,
    company_id  UUID    NOT NULL,
    FOREIGN KEY (building_id) REFERENCES buildings (id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS coworking_spaces_company_id_idx ON coworking_spaces (company_id);


CREATE TABLE IF NOT EXISTS item_types
(
    id          UUID    DEFAULT uuidv7() PRIMARY KEY,
    name        VARCHAR               NOT NULL,
    description VARCHAR,
    icon        VARCHAR,
    color       VARCHAR               NOT NULL,
    offsets     point[]               NOT NULL,
    bookable    BOOLEAN DEFAULT FALSE NOT NULL,
    company_id  UUID                  NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS items_company_id_idx ON item_types (company_id);


CREATE TABLE IF NOT EXISTS coworking_items
(
    id           UUID DEFAULT uuidv7() PRIMARY KEY,
    name         VARCHAR NOT NULL,
    description  VARCHAR,
    item_id      UUID    NOT NULL,
    base_point   point   NOT NULL,
    coworking_id UUID    NOT NULL,
    FOREIGN KEY (item_id) REFERENCES item_types (id) ON DELETE CASCADE,
    FOREIGN KEY (coworking_id) REFERENCES coworking_spaces (id) ON DELETE CASCADE
);

