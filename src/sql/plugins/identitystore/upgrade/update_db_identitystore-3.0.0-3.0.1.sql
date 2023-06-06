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
    nb_equal_attributes     int,
    nb_missing_attributes   int,
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