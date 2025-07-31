-- V1__init.sql

-- --------------------------------------------------------------------------------
-- 1. Users
-- --------------------------------------------------------------------------------
CREATE TABLE users
(
    id      UUID PRIMARY KEY,
    balance NUMERIC(10, 2) NOT NULL
        CONSTRAINT chk_users_balance CHECK (balance >= 0)
);

-- --------------------------------------------------------------------------------
-- 2. F1 Events
-- --------------------------------------------------------------------------------
CREATE TABLE events
(
    id               UUID PRIMARY KEY,
    name             TEXT         NOT NULL,
    year             INTEGER      NOT NULL,
    country          VARCHAR(100) NOT NULL,
    session_type     VARCHAR(50)  NOT NULL,
    winner_driver_id UUID NULL
);

-- --------------------------------------------------------------------------------
-- 3. Drivers
-- --------------------------------------------------------------------------------
CREATE TABLE drivers
(
    id        UUID PRIMARY KEY,
    full_name TEXT NOT NULL
);

-- --------------------------------------------------------------------------------
-- 4. External references for events
-- --------------------------------------------------------------------------------
CREATE TABLE event_external_ref
(
    id            UUID PRIMARY KEY,
    provider_name VARCHAR(50)  NOT NULL
        CONSTRAINT chk_event_ext_ref_provider
            CHECK (provider_name IN ('openf1', 'ergast')),
    external_id   VARCHAR(100) NOT NULL,
    event_id      UUID         NOT NULL
        REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT uq_event_ext_ref UNIQUE (provider_name, external_id)
);

-- --------------------------------------------------------------------------------
-- 5. External references for drivers
-- --------------------------------------------------------------------------------
CREATE TABLE driver_external_ref
(
    id            UUID PRIMARY KEY,
    provider_name VARCHAR(50)  NOT NULL
        CONSTRAINT chk_driver_ext_ref_provider
            CHECK (provider_name IN ('openf1', 'ergast')),
    external_id   VARCHAR(100) NOT NULL,
    driver_id     UUID         NOT NULL
        REFERENCES drivers (id) ON DELETE CASCADE,
    CONSTRAINT uq_driver_ext_ref UNIQUE (provider_name, external_id)
);

-- --------------------------------------------------------------------------------
-- 6. Persisted odds (market cache)
-- --------------------------------------------------------------------------------
CREATE TABLE event_odds
(
    id         UUID PRIMARY KEY,
    event_id   UUID        NOT NULL
        REFERENCES events (id) ON DELETE CASCADE,
    driver_id  UUID        NOT NULL
        REFERENCES drivers (id) ON DELETE CASCADE,
    odd        SMALLINT    NOT NULL
        CONSTRAINT chk_event_odds_value CHECK (odd IN (2, 3, 4)),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- --------------------------------------------------------------------------------
-- 7. Bets
-- --------------------------------------------------------------------------------
CREATE TABLE bets
(
    id            UUID PRIMARY KEY,
    user_id       UUID           NOT NULL
        REFERENCES users (id),
    event_id      UUID           NOT NULL
        REFERENCES events (id),
    event_odds_id UUID           NOT NULL
        REFERENCES event_odds (id),
    amount        NUMERIC(10, 2) NOT NULL
        CONSTRAINT chk_bets_amount CHECK (amount > 0),
    status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
        CONSTRAINT chk_bets_status CHECK (status IN ('PENDING', 'WON', 'LOST')),
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- --------------------------------------------------------------------------------
-- 8. Indexes
-- --------------------------------------------------------------------------------
CREATE INDEX idx_bets_event ON bets (event_id);
CREATE INDEX idx_bets_user ON bets (user_id);
CREATE INDEX idx_bets_event_odds ON bets (event_odds_id);

CREATE INDEX idx_event_odds_latest
    ON event_odds (event_id, driver_id, created_at DESC);
