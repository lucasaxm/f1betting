-- V8__seed_users.sql
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS name TEXT NOT NULL DEFAULT '';

INSERT INTO users (id, balance, name) VALUES
  ('8c215643-ddbb-4cbe-8d92-fbd93f09018a', 100.00, 'User 1'),
  ('baa6fe5d-adf0-4a17-8258-b1fcadb6646b', 100.00, 'User 2'),
  ('64253ea4-4bb0-4785-93ca-8a9a393d1089', 100.00, 'User 3'),
  ('83e7c4a2-eb69-4c3f-8732-8396a353c4dc', 100.00, 'User 4'),
  ('c273d495-dc79-46d2-9585-9d7a52072f2f', 100.00, 'User 5');

ALTER TABLE users ALTER COLUMN name DROP DEFAULT;
