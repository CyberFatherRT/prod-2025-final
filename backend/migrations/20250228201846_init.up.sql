-- Add up migration script here

CREATE EXTENSION IF NOT EXISTS btree_gist;

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
