ALTER TABLE identitystore_identity ADD COLUMN is_mon_paris_active smallint NOT NULL DEFAULT 0;
ALTER TABLE identitystore_identity ADD COLUMN expiration_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '36 MONTH';
