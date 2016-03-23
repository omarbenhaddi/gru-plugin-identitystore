INSERT INTO identitystore_attibutes_key (id_attribute_key, key_name, key_description, key_type) VALUES
	(1, 'birthdate', 'Date de naissance', 0),
	(2, 'birthplace', 'Lieu de naissance (Code INSEE de la commune ou vide pour l\'étranger)', 0),
	(3, 'phone', 'Téléphone', 0),
	(4, 'email', 'Email', 0);

INSERT INTO identitystore_attribute_certifier (id_attribute_certifier, name, description, logo) VALUES
	(1, 'FranceConnect', 'Service d\'identités de FranceConnect', ''),
	(2, 'Envoi d\'un code par SMS', 'Validation du numéro de mobile par envoi d\'un code par SMS', '');

INSERT INTO identitystore_identity (id_identity, connection_id, customer_id, given_name, family_name, gender, birthdate, birthplace, email, preferred_username, address, phone) VALUES
	(1, 'azerty', '123', 'John', 'Doe', 0, NULL, '', 'john.doe@nowhere.com', '', '', '');
