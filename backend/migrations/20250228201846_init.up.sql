-- Add up migration script here

CREATE OR REPLACE FUNCTION uuidv7() RETURNS UUID
AS
$$
SELECT ENCODE(
               SET_BIT(
                       SET_BIT(
                               OVERLAY(uuid_send(gen_random_uuid()) PLACING
                                       SUBSTRING(int8send((EXTRACT(EPOCH FROM CLOCK_TIMESTAMP()) * 1000)::BIGINT) FROM
                                                 3)
                                       FROM 1 FOR 6),
                               52, 1),
                       53, 1), 'hex')::UUID;
$$ LANGUAGE sql VOLATILE;


CREATE TABLE IF NOT EXISTS companies
(
    id     UUID DEFAULT uuidv7() PRIMARY KEY,
    name   VARCHAR(120) NOT NULL,
    avatar VARCHAR
);

CREATE TYPE ROLE AS ENUM ('admin', 'student', 'guest', 'verified_guest');

CREATE TABLE IF NOT EXISTS users
(
    id         UUID                         DEFAULT uuidv7() PRIMARY KEY,
    name       VARCHAR(120)        NOT NULL,
    surname    VARCHAR(120)        NOT NULL,
    email      VARCHAR(120) UNIQUE NOT NULL,
    password   VARCHAR(120)        NOT NULL,
    avatar     VARCHAR,
    company_id UUID                NOT NULL,
    role       ROLE                NOT NULL DEFAULT 'guest',
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

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

CREATE TABLE IF NOT EXISTS bookings
(
    id                 UUID DEFAULT uuidv7() PRIMARY KEY,
    user_id            UUID      NOT NULL,
    coworking_space_id UUID      NOT NULL,
    coworking_item_id  UUID      NOT NULL,
    time_start         timestamp NOT NULL,
    time_end           timestamp NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (coworking_space_id) REFERENCES coworking_spaces (id),
    FOREIGN KEY (coworking_item_id) REFERENCES coworking_items (id)
);

CREATE INDEX IF NOT EXISTS booking_user_id_idx ON bookings (user_id);
CREATE INDEX IF NOT EXISTS booking_coworking_space_id_idx ON bookings (coworking_space_id);
CREATE INDEX IF NOT EXISTS booking_coworking_items_id_idx ON bookings (coworking_item_id);

