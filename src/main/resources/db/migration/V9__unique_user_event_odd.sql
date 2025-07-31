-- V9__unique_user_event_odd.sql
ALTER TABLE bets
    ADD CONSTRAINT uq_bets_user_event_odd UNIQUE (user_id, event_odds_id);
