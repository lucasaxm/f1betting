-- V10__events_indexes.sql
CREATE INDEX IF NOT EXISTS idx_events_year ON events (year);
CREATE INDEX IF NOT EXISTS idx_events_type ON events (type);
CREATE INDEX IF NOT EXISTS idx_events_country_lower ON events (lower(country));
