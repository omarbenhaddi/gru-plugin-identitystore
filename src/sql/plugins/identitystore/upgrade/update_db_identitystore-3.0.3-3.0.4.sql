
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