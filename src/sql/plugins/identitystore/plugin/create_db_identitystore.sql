
--
-- Structure for table identitystore_identity
--

DROP TABLE IF EXISTS identitystore_identity;
CREATE TABLE identitystore_identity (
id_identity int(6) NOT NULL,
connection_id varchar(50) NULL,
customer_id varchar(50) NULL,
given_name varchar(50) NOT NULL default '',
family_name varchar(50) NOT NULL default '',
gender int(11) NOT NULL default '0',
birthdate date NULL,
birthplace varchar(50) NULL,
email varchar(255) NULL,
preferred_username varchar(50) NULL,
address varchar(255) NULL ,
phone varchar(50) NULL,
PRIMARY KEY (id_identity)
);

--
-- Structure for table identitystore_attribute
--

DROP TABLE IF EXISTS identitystore_attribute;
CREATE TABLE identitystore_attribute (
id_attribute int(6) NOT NULL,
name varchar(50) NOT NULL default '',
key_name varchar(50) NOT NULL default '',
description long varchar NULL ,
key_type int(11) NOT NULL default '0',
PRIMARY KEY (id_attribute)
);

--
-- Structure for table identitystore_identity_attribute
--

DROP TABLE IF EXISTS identitystore_identity_attribute;
CREATE TABLE identitystore_identity_attribute (
id_identity int(11) NOT NULL default '0',
id_attribute int(11) NOT NULL default '0',
attribute_value varchar(255) NOT NULL default '',
id_certification int(11) NOT NULL default '0',
id_file int(11) default '0',
PRIMARY KEY ( id_identity , id_attribute )
);

--
-- Structure for table identitystore_attribute_certifier
--

DROP TABLE IF EXISTS identitystore_attribute_certifier;
CREATE TABLE identitystore_attribute_certifier (
id_attribute_certifier int(6) NOT NULL,
name varchar(50) NOT NULL default '',
code varchar(50) NOT NULL default '',
description varchar(255) NOT NULL default '',
logo_file LONG VARBINARY NULL,
logo_mime_type VARCHAR(50) DEFAULT NULL,
PRIMARY KEY ( id_attribute_certifier ),
INDEX( code )
);

--
-- Structure for table identitystore_attribute_certificate
--

DROP TABLE IF EXISTS identitystore_attribute_certificate;
CREATE TABLE identitystore_attribute_certificate (
id_attribute_certificate int(6) NOT NULL,
id_certifier int(11) NOT NULL default '0',
certificate_date timestamp NOT NULL,
certificate_level int(11) NOT NULL default '0',
expiration_date date NOT NULL,
PRIMARY KEY (id_attribute_certificate)
);

--
-- Structure for table identitystore_client_application
--

DROP TABLE IF EXISTS identitystore_client_application;
CREATE TABLE identitystore_client_application (
id_client_app int(6) NOT NULL,
name varchar(50) NOT NULL,
code varchar(50) NOT NULL,
hash varchar(250),
PRIMARY KEY (id_client_app)
);

--
-- Structure for table identitystore_client_access_control_list
--

DROP TABLE IF EXISTS identitystore_attribute_right;
CREATE TABLE identitystore_attribute_right (
id_client_app int(6) NOT NULL,
id_attribute int(6) NOT NULL,
readable int(1) NOT NULL  default '0',
writable int(1) NOT NULL  default '0',
certifiable int(1) NOT NULL default '0',
PRIMARY KEY (id_client_app, id_attribute)
);


