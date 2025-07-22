-- V4__create_sync_status_table.sql
CREATE TABLE sync_status
(
    year        INTEGER PRIMARY KEY,
    last_synced TIMESTAMPTZ NOT NULL
);
