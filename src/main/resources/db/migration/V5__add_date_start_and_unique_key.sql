-- V5__add_date_start_and_unique_key.sql
-- Add date_start column and unique natural key on (country, date_start)
ALTER TABLE events
    ADD COLUMN date_start TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE UNIQUE INDEX uq_events_country_date_start ON events(country, date_start);
