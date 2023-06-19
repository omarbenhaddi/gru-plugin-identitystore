ALTER TABLE identitystore_identity ADD COLUMN is_mon_paris_active smallint NOT NULL DEFAULT 0;
ALTER TABLE identitystore_identity ADD COLUMN expiration_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '36 MONTH';

ALTER TABLE identitystore_ref_attribute ADD COLUMN mandatory_for_creation smallint NOT NULL DEFAULT 0;
UPDATE identitystore_ref_attribute SET mandatory_for_creation = 1 WHERE key_name IN ('family_name', 'first_name', 'birthdate');