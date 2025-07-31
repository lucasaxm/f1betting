-- V6__sync_status_per_provider.sql
DROP TABLE IF EXISTS sync_status;

CREATE TABLE sync_status (
    id UUID PRIMARY KEY,
    provider_name VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    last_synced TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_sync_status_provider_year UNIQUE (provider_name, year)
);
