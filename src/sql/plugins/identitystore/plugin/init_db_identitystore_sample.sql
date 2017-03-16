DELETE FROM identitystore_attribute_certifier;
DELETE FROM identitystore_identity_attribute;
DELETE FROM identitystore_attribute_right;
DELETE FROM identitystore_client_application;
DELETE FROM identitystore_attribute;
DELETE FROM identitystore_identity;

INSERT INTO identitystore_attribute (id_attribute, key_name, name, description, key_type) VALUES
	(1, 'gender', 'Genre', '0:Non défini 1:Homme 2:Femme', 0),
	(2, 'email', 'Email', '', 0),
	(3, 'birthdate', 'Date de naissance', 'au format DD/MM/YYYY', 0),
	(4, 'birthplace', 'Lieu de naissance', '', 0),
	(5, 'mobile_phone', 'Téléphone portable', 'Réservé pour l''envoi de SMS',  0),
	(6, 'fixed_phone', 'Téléphone fixe', '',0),
	(7, 'phone', 'Téléphone fixe ou mobile', '', 0),
	(8, 'preferred_username', 'Nom usuel','', 0),
	(9, 'address', 'Adresse postale', '', 0),
	(10, 'first_name', 'Prénom', 'Prénom usuel', 0),
	(11, 'family_name', 'Nom de famille de naissance', '', 0),
	(12, 'address_number','Numéro de rue','Champ d''adresse : numéro de rue ',0),
	(13, 'address_suffix','Suffixe','Champ d''adresse : suffixe de numéro (bis,ter...)',0),
	(14, 'address_street','Rue','Champ d''adresse :  rue, avenue...',0),
	(15, 'address_building','Immeuble','Champ d''adresse : immeuble, résidence...',0),
	(16, 'address_stair','Etage','Champ d''adresse : Etage, Numéro d appartement',0),
	(17, 'address_postal_code','Code postal','Champ d''adresse : code postal',0),
	(18, 'address_city','Ville','Champ d''adresse : ville',0),
        (30, 'fc_given_name' , '(FC) Prénoms' , 'FranceConnect - Liste des prénoms - Format Pivot', 0 ),
        (31, 'fc_family_name' , '(FC) Nom de naissance' , 'Format Pivot FranceConnect', 0 ),
        (32, 'fc_birthdate' , '(FC) Date de naissance' , 'Format Pivot FranceConnect - format YYYY-MM-DD', 0 ),
        (33, 'fc_gender' , '(FC) Genre' , 'Format Pivot FranceConnect - male / female', 0 ),
        (34, 'fc_birthplace' , '(FC) Lieu de naissance' , 'Format Pivot FranceConnect - Code INSEE du lieu de naissance (ou une chaîne vide si la personne est née à l\'étranger)', 0 ),
        (35, 'fc_birthcountry' , '(FC) Pays de naissance' , 'Format Pivot FranceConnect - Code INSEE du pays de naissance' , 0 );

INSERT INTO identitystore_identity (id_identity, connection_id, customer_id ) VALUES
	(1, 'azerty', '3F2504E0-4F89-11D3-9A0C-0305E82C3301' );

INSERT INTO identitystore_identity_attribute(id_identity,id_attribute,attribute_value,id_certification) VALUES
(1, 1, 'M', 0),
(1, 2, 'john.doe@gmail.com', 0),
(1, 3, '11/10/1970', 0),
(1, 4, 'Paris', 0),
(1, 5, '0623457896', 0),
(1, 6, '0123457896', 0),
(1, 7, '0123457896', 0),
(1, 8, 'Joe', 0),
(1, 9, 'Rue de Rennes', 0),
(1, 10, 'John', 0),
(1, 11, 'Doe', 0),
(1, 12, '8', 0),
(1, 13, 'Bis', 0),
(1, 14, 'Rue de Rennes', 0),
(1, 15, 'Escalier B', 0),
(1, 16, 'Etage 4', 0),
(1, 17, '75018', 0),
(1, 18, 'Paris', 0)
;

INSERT INTO identitystore_client_application (id_client_app, name, code) VALUES
(1, 'My Application', 'MyApplication');


INSERT INTO identitystore_attribute_right (id_client_app, id_attribute, readable, writable, certifiable) VALUES
(1, 1, 1, 1, 0), 
(1, 2, 1, 1, 0), 
(1, 3, 1, 1, 0), 
(1, 4, 1, 1, 0), 
(1, 5, 1, 1, 0), 
(1, 6, 1, 1, 0), 
(1, 7, 1, 1, 0), 
(1, 8, 1, 1, 0), 
(1, 9, 1, 1, 0),
(1, 10, 1, 1, 0),
(1, 11, 1, 1, 0),
(1, 12, 1, 1, 0),
(1, 13, 1, 1, 0),
(1, 14, 1, 1, 0),
(1, 15, 1, 1, 0),
(1, 16, 1, 1, 0),
(1, 17, 1, 1, 0),
(1, 18, 1, 1, 0)
;


