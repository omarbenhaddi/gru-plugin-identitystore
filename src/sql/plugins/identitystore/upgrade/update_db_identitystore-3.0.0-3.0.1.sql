--
-- Structure for table identitystore_identity_attribute_certificate
--
ALTER TABLE identitystore_identity_attribute_certificate DROP COLUMN certificate_level;

--
-- Structure for table identitystore_attribute
--
ALTER TABLE identitystore_ref_attribute ADD COLUMN common_search_key VARCHAR(100) NULL;
UPDATE identitystore_ref_attribute SET common_search_key = 'common_lastname' WHERE key_name IN ('family_name', 'preferred_username');
UPDATE identitystore_ref_attribute SET common_search_key = 'common_email' WHERE key_name IN ('email', 'login');
UPDATE identitystore_ref_attribute SET common_search_key = 'common_phone' WHERE key_name IN ('mobile_phone', 'fixed_phone');

--
-- Structure for table identitystore_client_application
--

ALTER TABLE identitystore_client_application ADD COLUMN application_code VARCHAR(255);
ALTER TABLE identitystore_client_application ALTER COLUMN code TYPE VARCHAR(255);
ALTER TABLE identitystore_client_application ALTER COLUMN name TYPE VARCHAR(255);
ALTER TABLE identitystore_client_application RENAME COLUMN code TO client_code;

--
-- Structure for table identitystore_identity_attribute
--
ALTER TABLE identitystore_identity_attribute RENAME COLUMN lastupdate_application TO lastupdate_client;

--
-- Structure for table identitystore_service_contract
--
ALTER TABLE identitystore_service_contract RENAME organizational_entity TO moa_entity_name;
COMMENT ON COLUMN identitystore_service_contract.moa_entity_name IS 'Direction/Bureau de la MOA';

ALTER TABLE identitystore_service_contract RENAME responsible_name TO moe_responsible_name;
COMMENT ON COLUMN identitystore_service_contract.moe_responsible_name IS 'Nom du responsable MOE';

ALTER TABLE identitystore_service_contract RENAME contact_name TO moa_contact_name;
COMMENT ON COLUMN identitystore_service_contract.moa_contact_name IS 'Nom du contact MOA';

ALTER TABLE identitystore_service_contract ADD COLUMN moe_entity_name VARCHAR(50) DEFAULT '' NOT NULL;
COMMENT ON COLUMN identitystore_service_contract.moe_entity_name IS 'Direction/Bureau de la MOE';

ALTER TABLE identitystore_service_contract ADD COLUMN data_retention_period_in_months INT DEFAULT 0 NOT NULL;
COMMENT ON COLUMN identitystore_service_contract.data_retention_period_in_months IS 'Durée de conservation CGU des données (en mois).  Doit être renseigné avec le nombre de mois de conservation des identités qu''il faut garantir au service numérique client.';

ALTER TABLE identitystore_service_contract ADD COLUMN authorized_creation SMALLINT DEFAULT 0;
COMMENT ON COLUMN identitystore_service_contract.authorized_creation IS 'Permet de définir si l''application pourra réaliser de la création d''identités. Il faut interdire les créations d''identités si cette case n''est pas cochée.';

ALTER TABLE identitystore_service_contract ADD COLUMN authorized_update SMALLINT DEFAULT 0;
COMMENT ON COLUMN identitystore_service_contract.authorized_update IS 'Permet de définir si l''application pourra réaliser de la modification d''identités.  Il faut interdire les modifications d''identités si cette case n''est pas cochée.';

COMMENT ON COLUMN identitystore_service_contract.authorized_search IS 'Permet de définir si l''application pourra réaliser de la recherche d’identités.';

ALTER TABLE identitystore_service_contract DROP COLUMN authorized_read;
ALTER TABLE identitystore_service_contract DROP COLUMN is_application_authorized_to_delete_certificate;
ALTER TABLE identitystore_service_contract DROP COLUMN is_application_authorized_to_delete_value;

--
-- Structure for table identitystore_service_contract_attribute_right
--
ALTER TABLE identitystore_service_contract_attribute_right ADD COLUMN mandatory SMALLINT NOT NULL DEFAULT 0;

--
-- Structure for table identitystore_duplicate_rule
--
CREATE TABLE identitystore_duplicate_rule (
    id_rule                 int AUTO_INCREMENT,
    name                    varchar(100) NOT NULL UNIQUE,
    description             varchar,
    nb_filled_attributes    int,
    nb_equal_attributes     int,
    nb_missing_attributes   int,
    priority varchar(30) NOT NULL DEFAULT 'LEVEL3',
    active smallint NOT NULL DEFAULT 0,
    daemon smallint NOT NULL DEFAULT 0,
    PRIMARY KEY (id_rule)
);


--
-- Structure for table identitystore_duplicate_rule_checked_attributes
--
CREATE TABLE identitystore_duplicate_rule_checked_attributes (
    id_rule      int,
    id_attribute int
);
ALTER TABLE identitystore_duplicate_rule_checked_attributes
    ADD CONSTRAINT fk_duplicate_rule_checked_attributes_id_rule FOREIGN KEY (id_rule) REFERENCES identitystore_duplicate_rule (id_rule) ON DELETE CASCADE;
ALTER TABLE identitystore_duplicate_rule_checked_attributes
    ADD CONSTRAINT fk_duplicate_rule_checked_attributes_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);

--
-- Structure for table identitystore_duplicate_rule_attribute_treatment
--
CREATE TABLE identitystore_duplicate_rule_attribute_treatment (
    id_attribute_treatment  int AUTO_INCREMENT,
    type                    varchar(100) NOT NULL,
    id_rule                 int,
    PRIMARY KEY (id_attribute_treatment)
);
ALTER TABLE identitystore_duplicate_rule_attribute_treatment
    ADD CONSTRAINT fk_duplicate_rule_attribute_treatment_id_rule FOREIGN KEY (id_rule) REFERENCES identitystore_duplicate_rule (id_rule) ON DELETE CASCADE;

--
-- Structure for table identitystore_duplicate_rule_attribute_treatment_nuples
--
CREATE TABLE identitystore_duplicate_rule_attribute_treatment_nuples (
    id_attribute_treatment  int,
    id_attribute            int
);
ALTER TABLE identitystore_duplicate_rule_attribute_treatment_nuples
    ADD CONSTRAINT fk_duplicate_rule_attribute_treatment_nuples_id_rule FOREIGN KEY (id_attribute_treatment) REFERENCES identitystore_duplicate_rule_attribute_treatment (id_attribute_treatment) ON DELETE CASCADE;
ALTER TABLE identitystore_duplicate_rule_attribute_treatment_nuples
    ADD CONSTRAINT fk_duplicate_rule_attribute_treatment_nuples_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);

--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'IDENTITYSTORE_DUPLICATE_RULES_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES
    ('IDENTITYSTORE_DUPLICATE_RULES_MANAGEMENT','identitystore.adminFeature.ManageDuplicateRules.name',1,'jsp/admin/plugins/identitystore/ManageDuplicateRules.jsp','identitystore.adminFeature.ManageDuplicateRules.description',0,'identitystore',NULL,NULL,NULL,4);

--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'IDENTITYSTORE_DUPLICATE_RULES_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('IDENTITYSTORE_DUPLICATE_RULES_MANAGEMENT',1);


ALTER TABLE identitystore_identity ADD COLUMN is_mon_paris_active smallint NOT NULL DEFAULT 0;
ALTER TABLE identitystore_identity ADD COLUMN expiration_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '36 MONTH';

ALTER TABLE identitystore_ref_attribute ADD COLUMN mandatory_for_creation smallint NOT NULL DEFAULT 0;
UPDATE identitystore_ref_attribute SET mandatory_for_creation = 1 WHERE key_name IN ('family_name', 'first_name', 'birthdate');



DROP TABLE IF EXISTS identitystore_identity_search_rule_attribute;
DROP TABLE IF EXISTS identitystore_identity_search_rule;

CREATE TABLE identitystore_identity_search_rule (
    id_rule      int             AUTO_INCREMENT,
    type         varchar(8)      NOT NULL,
    PRIMARY KEY (id_rule)
);

CREATE TABLE identitystore_identity_search_rule_attribute (
    id_rule        int     NOT NULL,
    id_attribute   int     NOT NULL
);
ALTER TABLE identitystore_identity_search_rule_attribute
    ADD CONSTRAINT fk_identity_search_rule_attribute_id_rule FOREIGN KEY (id_rule) REFERENCES identitystore_identity_search_rule (id_rule) ON DELETE CASCADE;
ALTER TABLE identitystore_identity_search_rule_attribute
    ADD CONSTRAINT fk_identity_search_rule_attribute_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);

INSERT INTO identitystore_identity_search_rule (type) VALUES ('OR');
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'login' GROUP BY a.id_attribute;
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'email' GROUP BY a.id_attribute;

INSERT INTO identitystore_identity_search_rule (type) VALUES ('AND');
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'family_name' GROUP BY a.id_attribute;
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'first_name' GROUP BY a.id_attribute;
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'birthdate' GROUP BY a.id_attribute;

--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'IDENTITYSTORE_IDENTITY_SEARCH_RULES_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES
    ('IDENTITYSTORE_IDENTITY_SEARCH_RULES_MANAGEMENT','identitystore.adminFeature.ManageIdentitySearchRules.name',1,'jsp/admin/plugins/identitystore/ManageIdentitySearchRules.jsp','identitystore.adminFeature.ManageIdentitySearchRules.description',0,'identitystore',NULL,NULL,NULL,4);

--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'IDENTITYSTORE_IDENTITY_SEARCH_RULES_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('IDENTITYSTORE_IDENTITY_SEARCH_RULES_MANAGEMENT',1);


--
-- Structure for table identitystore_identity_history
--
CREATE TABLE  identitystore_identity_history
(
    id_history            int AUTO_INCREMENT,
    change_type           int not null,
    change_status         varchar(255),
    change_message        varchar(255),
    author_type           varchar(255),
    author_name           varchar(255),
    client_code           varchar(255),
    customer_id           varchar(50) NOT NULL,
    modification_date     timestamp    default CURRENT_TIMESTAMP,
    PRIMARY KEY (id_history)
    );

CREATE INDEX identitystore_identity_history_cuid ON identitystore_identity_history (customer_id);


-- add attribute validation columns
ALTER TABLE identitystore_ref_attribute ADD COLUMN validation_regex varchar(510) DEFAULT '^[A-Za-zÀ-Üà-ü\d\s''-]+$';
ALTER TABLE identitystore_ref_attribute ADD COLUMN validation_error_message varchar(255) DEFAULT 'uniquement caractères alphanumériques, apostrophe, espace et tirets.';

-- add attribute validation data
UPDATE identitystore_ref_attribute SET validation_regex = '^[A-Za-zÀ-Üà-ü\s''-]+$', validation_error_message = 'Uniquement caractères aplha, apostrophe, espace et tirets.' WHERE key_name IN ('family_name', 'preferred_username', 'first_name');
UPDATE identitystore_ref_attribute SET validation_regex = '^(?:(?:31(\/)(?:0[13578]|1[02]))\1|(?:(?:29|30)(\/)(?:0[13-9]|1[0-2])\2))(?:(?:1[6-9]|[2-9]\d)?\d{2})$|^(?:29(\/)02\3(?:(?:(?:1[6-9]|[2-9]\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0[1-9]|1\d|2[0-8])(\/)(?:(?:0[1-9])|(?:1[0-2]))\4(?:(?:1[6-9]|[2-9]\d)?\d{2})$', validation_error_message = 'Date exprimée sur 10 caractères en jj/mm/aaaa.' WHERE key_name = 'birthdate';
UPDATE identitystore_ref_attribute SET validation_regex = '^0(6|7)\d{8}$', validation_error_message = 'uniquement numérique, numéro français sur 10 chiffres commençant par O6 ou 07.'  WHERE key_name = 'mobile_phone';
UPDATE identitystore_ref_attribute SET validation_regex = '^0([1-5]|9)\d{8}$', validation_error_message = 'uniquement numérique, numéro français sur 10 chiffres commençant par 01 à 05 ou 09.'  WHERE key_name = 'fixed_phone';
UPDATE identitystore_ref_attribute SET validation_regex = '^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$', validation_error_message = 'format mail du type xxx@yyy.zzz'  WHERE key_name = 'email';
UPDATE identitystore_ref_attribute SET validation_regex = '^[A-Z\d]{5}$', validation_error_message = 'Alphanumérique sur 5 caractères. Le code doit exister dans notre référentiel géocode'  WHERE key_name IN ('birthplace_code', 'birthcountry_code');
UPDATE identitystore_ref_attribute SET validation_regex = '^[A-Z\s]+$', validation_error_message = 'uniquement caractères aplha en majuscule non accentué, et espace.'  WHERE key_name IN ('birthplace', 'birthcountry');
UPDATE identitystore_ref_attribute SET validation_regex = '^[0-2]{1}$', validation_error_message = 'uniquement  0, 1 ou 2'  WHERE key_name = 'gender';

-- identitystore_duplicate_rule #291 change priority to INT
ALTER TABLE identitystore_duplicate_rule
    ALTER COLUMN priority DROP DEFAULT,
ALTER COLUMN priority TYPE INT USING CASE WHEN priority = 'LEVEL1' THEN 1 WHEN priority = 'LEVEL2' THEN 2 WHEN priority = 'LEVEL3' THEN 3 END,
    ALTER COLUMN priority SET NOT NULL,
    ALTER COLUMN priority SET DEFAULT 100;

-- identitystore_identity #223 when creating a new identity, fill last_update_date with same value as date_create
UPDATE identitystore_identity SET last_update_date = date_create WHERE last_update_date IS NULL;

ALTER TABLE identitystore_identity ALTER COLUMN last_update_date SET NOT NULL,
                                   ALTER COLUMN last_update_date SET DEFAULT CURRENT_TIMESTAMP;

-- identitystore_duplicate_rule #298 ajouter un champ code
ALTER TABLE identitystore_duplicate_rule
DROP CONSTRAINT identitystore_duplicate_rule_name_key,
    ADD COLUMN code varchar(100) UNIQUE ;
UPDATE identitystore_duplicate_rule SET code = name WHERE 1=1;

--
-- Structure for table identitystore_identity_history
--
ALTER TABLE identitystore_identity_history
    ADD COLUMN metadata json default NULL;

--
-- Structure for table identitystore_identity_attribute_history
--
ALTER TABLE identitystore_identity_attribute_history
    ADD COLUMN metadata json default NULL;


-- identitystore_duplicate_rule #293 ajouter un champ daemon_last_exec_date
ALTER TABLE identitystore_duplicate_rule ADD COLUMN daemon_last_exec_date TIMESTAMP DEFAULT NULL;


-- identitystore_service_contract #127 decertification d'une identité
ALTER TABLE identitystore_service_contract ADD COLUMN authorized_decertification SMALLINT NOT NULL DEFAULT 0;
COMMENT ON COLUMN identitystore_service_contract.authorized_decertification IS 'Permet de définir si l''application pourra réaliser des décertifications d''identités.  Il faut interdire les décertifications d''identités si cette case n''est pas cochée.';


--
-- Structure for table identitystore_service_contract
--
ALTER TABLE identitystore_service_contract
    ADD COLUMN authorized_agent_history_read smallint not null default 0;