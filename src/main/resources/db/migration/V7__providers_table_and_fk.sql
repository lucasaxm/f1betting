-- V7__providers_table_and_fk.sql
CREATE TABLE providers (
    id   UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO providers (id, name) VALUES
  (gen_random_uuid(), 'openf1');

-- driver_external_ref
ALTER TABLE driver_external_ref DROP CONSTRAINT chk_driver_ext_ref_provider;
ALTER TABLE driver_external_ref ADD COLUMN provider_id UUID;
UPDATE driver_external_ref d SET provider_id = p.id FROM providers p WHERE p.name = d.provider_name;
ALTER TABLE driver_external_ref ALTER COLUMN provider_id SET NOT NULL;
ALTER TABLE driver_external_ref ADD CONSTRAINT fk_driver_ext_ref_provider FOREIGN KEY (provider_id) REFERENCES providers(id);
ALTER TABLE driver_external_ref DROP CONSTRAINT uq_driver_ext_ref;
ALTER TABLE driver_external_ref ADD CONSTRAINT uq_driver_ext_ref UNIQUE (provider_id, external_id);
ALTER TABLE driver_external_ref DROP COLUMN provider_name;

-- event_external_ref
ALTER TABLE event_external_ref DROP CONSTRAINT chk_event_ext_ref_provider;
ALTER TABLE event_external_ref ADD COLUMN provider_id UUID;
UPDATE event_external_ref e SET provider_id = p.id FROM providers p WHERE p.name = e.provider_name;
ALTER TABLE event_external_ref ALTER COLUMN provider_id SET NOT NULL;
ALTER TABLE event_external_ref ADD CONSTRAINT fk_event_ext_ref_provider FOREIGN KEY (provider_id) REFERENCES providers(id);
ALTER TABLE event_external_ref DROP CONSTRAINT uq_event_ext_ref;
ALTER TABLE event_external_ref ADD CONSTRAINT uq_event_ext_ref UNIQUE (provider_id, external_id);
ALTER TABLE event_external_ref DROP COLUMN provider_name;

-- sync_status
ALTER TABLE sync_status ADD COLUMN provider_id UUID;
UPDATE sync_status s SET provider_id = p.id FROM providers p WHERE p.name = s.provider_name;
ALTER TABLE sync_status ALTER COLUMN provider_id SET NOT NULL;
ALTER TABLE sync_status ADD CONSTRAINT fk_sync_status_provider FOREIGN KEY (provider_id) REFERENCES providers(id);
ALTER TABLE sync_status DROP CONSTRAINT uq_sync_status_provider_year;
ALTER TABLE sync_status ADD CONSTRAINT uq_sync_status_provider_year UNIQUE (provider_id, year);
ALTER TABLE sync_status DROP COLUMN provider_name;
