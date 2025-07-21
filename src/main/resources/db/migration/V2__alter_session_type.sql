-- V2__alter_session_type.sql
-- Rename 'session_type' column to 'type' in 'events' table
ALTER TABLE events
    RENAME COLUMN session_type TO type;
