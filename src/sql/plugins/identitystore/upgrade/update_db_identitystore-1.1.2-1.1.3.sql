ALTER TABLE identitystore_history_identity_attribute CHANGE COLUMN identity_connection_id identity_connection_id VARCHAR(100) NULL DEFAULT NULL COLLATE utf8_unicode_ci AFTER id_identity;

ALTER TABLE identitystore_attribute CHANGE COLUMN name name varchar(100) NOT NULL default '' ;
ALTER TABLE identitystore_attribute CHANGE COLUMN key_name key_name varchar(100) NOT NULL default '' ;
