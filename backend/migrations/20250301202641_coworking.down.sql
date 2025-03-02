-- Add down migration script here

DROP TABLE IF EXISTS coworking_items;
DROP TABLE IF EXISTS item_types;
DROP TABLE IF EXISTS coworking_spaces;

DROP TYPE IF EXISTS point;
