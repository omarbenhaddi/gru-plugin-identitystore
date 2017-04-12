DROP TABLE IF EXISTS identitystore_attribute_certifier;
ALTER TABLE identitystore_attribute_certificate CHANGE COLUMN id_certifier certifier_code varchar(255) NOT NULL default '';

--
-- Structure for table identitystore_client_application_certifiers
--
DROP TABLE IF EXISTS identitystore_client_application_certifiers;
CREATE TABLE identitystore_client_application_certifiers (
id_client_app int(6) NOT NULL,
certifier_code varchar(255) NOT NULL default '',
PRIMARY KEY (id_client_app, certifier_code),
INDEX (id_client_app)
);

ALTER TABLE identitystore_attribute_certificate CHANGE COLUMN expiration_date expiration_date TIMESTAMP NULL DEFAULT NULL;
