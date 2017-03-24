DROP TABLE IF EXISTS identitystore_attribute_certifier;
ALTER TABLE identitystore_attribute_certificate CHANGE COLUMN id_certifier certifier_code varchar(255) NOT NULL default '';