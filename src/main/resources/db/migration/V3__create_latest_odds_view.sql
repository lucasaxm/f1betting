-- V3__create_latest_odds_view.sql
-- Create view for latest odds per driver
CREATE VIEW current_event_odds_view AS
SELECT DISTINCT ON (driver_id) id,
                               event_id,
                               driver_id,
                               odd,
                               created_at AS updated_at
FROM event_odds
ORDER BY driver_id, created_at DESC;
