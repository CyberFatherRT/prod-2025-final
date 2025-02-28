-- Add down migration script here

DROP INDEX IF EXISTS booking_coworking_space_id_idx;
DROP INDEX IF EXISTS booking_coworking_items_id_idx;
DROP INDEX IF EXISTS booking_user_id_idx;

DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS coworking_items;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS coworking_spaces;

DROP TYPE IF EXISTS ROLE;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS companies;

DROP FUNCTION IF EXISTS uuidv7();
