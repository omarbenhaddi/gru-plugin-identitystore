ALTER TABLE identitystore_identity ADD COLUMN is_deleted SMALLINT default 0;
ALTER TABLE identitystore_identity ADD COLUMN date_delete TIMESTAMP NULL DEFAULT NULL;