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

INSERT INTO identitystore_attribute (id_attribute, key_name, name, description, key_type) SELECT MAX(id_attribute) +1 , 'fc_given_name' , '(FC) Prénoms' , 'Format Pivot FranceConnect - Liste des prénoms', 0 FROM identitystore_attribute;
INSERT INTO identitystore_attribute (id_attribute, key_name, name, description, key_type) SELECT MAX(id_attribute) +1 , 'fc_family_name' , '(FC) Nom de naissance' , 'Format Pivot FranceConnect', 0 FROM identitystore_attribute;
INSERT INTO identitystore_attribute (id_attribute, key_name, name, description, key_type) SELECT MAX(id_attribute) +1 , 'fc_birthdate' , '(FC) Date de naissance' , 'Format Pivot FranceConnect - format YYYY-MM-DD', 0 FROM identitystore_attribute;
INSERT INTO identitystore_attribute (id_attribute, key_name, name, description, key_type) SELECT MAX(id_attribute) +1 , 'fc_gender' , '(FC) Genre' , 'Format Pivot FranceConnect - male / female', 0 FROM identitystore_attribute;
INSERT INTO identitystore_attribute (id_attribute, key_name, name, description, key_type) SELECT MAX(id_attribute) +1 , 'fc_birthplace' , '(FC) Lieu de naissance' , 'Format Pivot FranceConnect - Code INSEE du lieu de naissance (ou une chaîne vide si la personne est née à l\'étranger)', 0 FROM identitystore_attribute;
INSERT INTO identitystore_attribute (id_attribute, key_name, name, description, key_type) SELECT MAX(id_attribute) +1 , 'fc_birthcountry' , '(FC) Pays de naissance' , 'Format Pivot FranceConnect - Code INSEE du pays de naissance' , 0 FROM identitystore_attribute;