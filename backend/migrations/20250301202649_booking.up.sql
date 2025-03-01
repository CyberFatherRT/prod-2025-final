-- Add up migration script here

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

