ALTER TABLE identitystore_identity_attribute_history  CHANGE COLUMN identity_connection_id identity_connection_id VARCHAR(100) NULL DEFAULT NULL COLLATE utf8_unicode_ci AFTER id_identity;

ALTER TABLE identitystore_ref_attribute CHANGE COLUMN name name varchar(100) NOT NULL default '' ;
ALTER TABLE identitystore_ref_attribute CHANGE COLUMN key_name key_name varchar(100) NOT NULL default '' ;

ALTER TABLE identitystore_client_application CHANGE COLUMN name name varchar(100) NOT NULL default '' ;
ALTER TABLE identitystore_client_application CHANGE COLUMN code code varchar(100) NOT NULL default '' ;
ALTER TABLE identitystore_client_application DROP COLUMN control_key ;
ALTER TABLE identitystore_client_application DROP COLUMN hash;