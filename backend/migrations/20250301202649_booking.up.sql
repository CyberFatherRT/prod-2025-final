-- Add up migration script here

CREATE TABLE IF NOT EXISTS bookings
(
    id                 UUID DEFAULT uuidv7() PRIMARY KEY,
    user_id            UUID      NOT NULL,
    coworking_space_id UUID      NOT NULL,
    coworking_item_id  UUID      NOT NULL,
    company_id         UUID      NOT NULL,
    time_start         timestamp NOT NULL,
    time_end           timestamp NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (coworking_space_id) REFERENCES coworking_spaces (id) ON DELETE CASCADE,
    FOREIGN KEY (coworking_item_id) REFERENCES coworking_items (id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
    EXCLUDE USING gist (
        coworking_item_id WITH =,
        tsrange(time_start, time_end) WITH &&
        ),
    CHECK (bookings.time_start < bookings.time_end),
    CHECK (EXTRACT(EPOCH FROM time_end - time_start)::integer % 900 = 0),
    CHECK (bookings.time_start > NOW())
);

CREATE INDEX IF NOT EXISTS booking_user_id_idx ON bookings (user_id);
CREATE INDEX IF NOT EXISTS booking_coworking_space_id_idx ON bookings (coworking_space_id);
CREATE INDEX IF NOT EXISTS booking_coworking_items_id_idx ON bookings (coworking_item_id);
CREATE INDEX IF NOT EXISTS booking_company_id_idx ON bookings (company_id);
