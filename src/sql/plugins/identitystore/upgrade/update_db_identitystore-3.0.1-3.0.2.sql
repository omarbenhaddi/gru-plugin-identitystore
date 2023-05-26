ALTER TABLE identitystore_duplicate_rule ADD COLUMN priority varchar(30) NOT NULL DEFAULT 'LEVEL3';
ALTER TABLE identitystore_duplicate_rule ADD COLUMN active smallint NOT NULL DEFAULT 0;
ALTER TABLE identitystore_duplicate_rule ADD COLUMN daemon smallint NOT NULL DEFAULT 0;