-- Add down migration script here

DROP INDEX IF EXISTS booking_coworking_items_id_idx;
DROP INDEX IF EXISTS booking_coworking_space_id_idx;
DROP INDEX IF EXISTS booking_user_id_idx;

DROP TABLE IF EXISTS booking;
