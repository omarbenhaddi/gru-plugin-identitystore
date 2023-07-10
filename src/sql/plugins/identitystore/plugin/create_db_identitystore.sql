--
-- Drop tables if exists
--


DROP TABLE IF EXISTS identitystore_service_contract_attribute_certification CASCADE;
DROP TABLE IF EXISTS identitystore_identity CASCADE;
DROP TABLE IF EXISTS identitystore_service_contract_attribute_requirement CASCADE;
DROP TABLE IF EXISTS identitystore_service_contract_attribute_right CASCADE;
DROP TABLE IF EXISTS identitystore_service_contract CASCADE;
DROP TABLE IF EXISTS identitystore_identity_attribute_history  CASCADE;
DROP TABLE IF EXISTS identitystore_client_application CASCADE;
DROP TABLE IF EXISTS identitystore_client_application_certifiers CASCADE;
DROP TABLE IF EXISTS identitystore_identity_attribute_certificate CASCADE;
DROP TABLE IF EXISTS identitystore_identity_attribute CASCADE;
DROP TABLE IF EXISTS identitystore_ref_certification_processus CASCADE;
DROP TABLE IF EXISTS identitystore_ref_certification_attribute_level CASCADE;
DROP TABLE IF EXISTS identitystore_ref_certification_level CASCADE;
DROP TABLE IF EXISTS identitystore_ref_attribute CASCADE;
DROP TABLE IF EXISTS identitystore_identity CASCADE;
--
-- Structure for table identitystore_identity
--

CREATE TABLE identitystore_identity
(
    id_identity        int AUTO_INCREMENT,
    connection_id      varchar(100) NULL UNIQUE,
    customer_id        varchar(50)  NOT NULL UNIQUE,
    date_create        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_update_date   timestamp             DEFAULT NULL,
    is_deleted         smallint              default 0,
    date_delete        timestamp    NULL,
    is_merged          smallint              default 0,
    date_merge         timestamp    NULL,
    id_master_identity int          NULL,
    is_mon_paris_active smallint NOT NULL DEFAULT 0,
    expiration_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '36 MONTH',
    PRIMARY KEY (id_identity)
);

CREATE INDEX identitystore_identity_connection_id ON identitystore_identity (connection_id);
CREATE INDEX identitystore_identity_customer_id ON identitystore_identity (customer_id);

--
-- Structure for table identitystore_attribute
--

CREATE TABLE identitystore_ref_attribute
(
    id_attribute int AUTO_INCREMENT,
    name         varchar(100) NOT NULL default '' UNIQUE,
    key_name     varchar(100) NOT NULL default '' UNIQUE,
    description  varchar(255) NULL,
    key_type     int          NOT NULL default '0',
    key_weight   int          NOT NULL default '0',
    certifiable  smallint              default 0,
    pivot        smallint              default 0,
    mandatory_for_creation smallint NOT NULL DEFAULT 0,
    PRIMARY KEY (id_attribute)
);

-- Matrice de configuration des processus

CREATE TABLE identitystore_ref_certification_level
(
    id_ref_certification_level int AUTO_INCREMENT,
    name                       varchar(255) default '',
    description                varchar(255) default '',
    level                      varchar(50)  default '' NOT NULL,
    PRIMARY KEY (id_ref_certification_level)
);

CREATE TABLE identitystore_ref_certification_processus
(
    id_ref_attribute_certification_processus int AUTO_INCREMENT,
    label                                    varchar(50) default '' NOT NULL,
    code                                     varchar(50) default '' NOT NULL,
    PRIMARY KEY (id_ref_attribute_certification_processus)
);

CREATE TABLE identitystore_ref_certification_attribute_level
(
    id_attribute                             int NOT NULL,
    id_ref_certification_level               int NOT NULL,
    id_ref_attribute_certification_processus int NOT NULL,
    PRIMARY KEY (id_attribute, id_ref_certification_level, id_ref_attribute_certification_processus)
);
ALTER TABLE identitystore_ref_certification_attribute_level
    ADD CONSTRAINT fk_attribute_ref_certification_level_certification_level FOREIGN KEY (id_ref_certification_level) REFERENCES identitystore_ref_certification_level (id_ref_certification_level);
ALTER TABLE identitystore_ref_certification_attribute_level
    ADD CONSTRAINT fk_attribute_ref_certification_level_certification_processus FOREIGN KEY (id_ref_attribute_certification_processus) REFERENCES identitystore_ref_certification_processus (id_ref_attribute_certification_processus);
ALTER TABLE identitystore_ref_certification_attribute_level
    ADD CONSTRAINT fk_attribute_ref_certification_level_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);

--
-- Structure for table identitystore_identity_attribute
--

CREATE TABLE identitystore_identity_attribute
(
    id_identity            int          NOT NULL default '0',
    id_attribute           int          NOT NULL default '0',
    attribute_value        varchar(100) NULL,
    id_certification       int          NOT NULL default '0',
    id_file                int                   default '0',
    lastupdate_date        timestamp    NOT NULL default CURRENT_TIMESTAMP,
    lastupdate_application VARCHAR(100) NULL,
    PRIMARY KEY (id_identity, id_attribute)
);
ALTER TABLE identitystore_identity_attribute
    ADD CONSTRAINT fk_identity_attribute_id_identity FOREIGN KEY (id_identity) REFERENCES identitystore_identity (id_identity);
ALTER TABLE identitystore_identity_attribute
    ADD CONSTRAINT fk_identity_attribute_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);
CREATE INDEX ix_attribute_value ON identitystore_identity_attribute (attribute_value ASC);

--
-- Structure for table identitystore_identity_attribute_certificate
--

CREATE TABLE identitystore_identity_attribute_certificate
(
    id_attribute_certificate int AUTO_INCREMENT,
    certifier_code           varchar(255) NOT NULL default '',
    certificate_date         timestamp    NOT NULL,
    certificate_level        int          NOT NULL default '0',
    expiration_date          timestamp    NULL     default NULL,
    PRIMARY KEY (id_attribute_certificate)
);

--
-- Structure for table identitystore_client_application_certifiers
--

CREATE TABLE identitystore_client_application_certifiers
(
    id_client_app  int          NOT NULL,
    certifier_code varchar(255) NOT NULL default '',
    PRIMARY KEY (id_client_app, certifier_code)
);
CREATE INDEX identitystore_client_application_certifiers_id_client_app ON identitystore_client_application_certifiers (id_client_app);


--
-- Structure for table identitystore_client_application
--

CREATE TABLE identitystore_client_application
(
    id_client_app int AUTO_INCREMENT,
    name          varchar(100) NOT NULL UNIQUE,
    code          varchar(100) NOT NULL UNIQUE,
    PRIMARY KEY (id_client_app)
);

--
-- Structure for table identitystore_identity_attribute_history 
--

CREATE TABLE identitystore_identity_attribute_history
(
    id_history            int AUTO_INCREMENT,
    change_type           int          NOT NULL,
    change_satus          varchar(255) NOT NULL,
    change_message        varchar(255)          default null,
    author_type           varchar(255) NOT NULL,
    author_name           varchar(255)          default null,
    client_code           varchar(255)          default null,
    id_identity           int          NOT NULL,
    attribute_key         varchar(50)  NOT NULL,
    attribute_value       varchar(255)          default null,
    certification_process varchar(255)          default null,
    certification_date    timestamp             default null,
    modification_date     timestamp    NOT NULL default CURRENT_TIMESTAMP,
    PRIMARY KEY (id_history)
);
ALTER TABLE identitystore_identity_attribute_history
    ADD CONSTRAINT fk_history_identity_attribute_id_identity FOREIGN KEY (id_identity) REFERENCES identitystore_identity (id_identity);

--
-- Structure for table identitystore_service_contract
--
CREATE TABLE identitystore_service_contract
(
    id_service_contract                             int AUTO_INCREMENT,
    id_client_app                                   int      NOT NULL,
    name                                            varchar(50)       default '' NOT NULL,
    organizational_entity                           varchar(50)       default '' NOT NULL,
    responsible_name                                varchar(50)       default '' NOT NULL,
    contact_name                                    varchar(50)       default '' NOT NULL,
    service_type                                    varchar(50)       default '' NOT NULL,
    starting_date                                   date     NOT NULL,
    ending_date                                     date              default NULL,
    authorized_read                                 smallint not null default 0,
    authorized_deletion                             smallint not null default 0,
    authorized_search                               smallint not null default 0,
    authorized_import                               smallint not null default 0,
    authorized_export                               smallint not null default 0,
    authorized_merge                                smallint not null default 0,
    authorized_account_update                       smallint not null default 0,
    is_application_authorized_to_delete_value       smallint not null default 0,
    is_application_authorized_to_delete_certificate smallint not null default 0,
    PRIMARY KEY (id_service_contract)
);
ALTER TABLE identitystore_service_contract
    ADD CONSTRAINT fk_service_contract_id_client_app FOREIGN KEY (id_client_app) REFERENCES identitystore_client_application (id_client_app);

--
-- Structure for table identitystore_client_access_control_list
--

CREATE TABLE identitystore_service_contract_attribute_right
(
    id_service_contract int      NOT NULL,
    id_attribute        int      NOT NULL,
    searchable          smallint NOT NULL default 0,
    readable            smallint NOT NULL default 0,
    writable            smallint NOT NULL default 0,
    PRIMARY KEY (id_service_contract, id_attribute)
);
ALTER TABLE identitystore_service_contract_attribute_right
    ADD CONSTRAINT fk_attribute_right_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);
ALTER TABLE identitystore_service_contract_attribute_right
    ADD CONSTRAINT fk_attribute_right_id_service_contract FOREIGN KEY (id_service_contract) REFERENCES identitystore_service_contract (id_service_contract);


--
-- Structure for table identitystore_service_contract_attribute_requirement
--
CREATE TABLE identitystore_service_contract_attribute_requirement
(
    id_service_contract        int NOT NULL,
    id_attribute               int NOT NULL,
    id_ref_certification_level int NOT NULL,
    PRIMARY KEY (id_attribute, id_service_contract, id_ref_certification_level)
);
ALTER TABLE identitystore_service_contract_attribute_requirement
    ADD CONSTRAINT fk_attribute_requirement_id_service_contract FOREIGN KEY (id_service_contract) REFERENCES identitystore_service_contract (id_service_contract);
ALTER TABLE identitystore_service_contract_attribute_requirement
    ADD CONSTRAINT fk_attribute_requirement_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);
ALTER TABLE identitystore_service_contract_attribute_requirement
    ADD CONSTRAINT fk_attribute_requirement_certification_level FOREIGN KEY (id_ref_certification_level) REFERENCES identitystore_ref_certification_level (id_ref_certification_level);

--
-- Structure for table identitystore_service_contract_attribute_certification
--

CREATE TABLE identitystore_service_contract_attribute_certification
(
    id_service_contract                      int NOT NULL,
    id_attribute                             int NOT NULL,
    id_ref_attribute_certification_processus int NOT NULL,
    PRIMARY KEY (id_attribute, id_ref_attribute_certification_processus, id_service_contract)
);
ALTER TABLE identitystore_service_contract_attribute_certification
    ADD CONSTRAINT fk_attribute_certification_id_service_contract FOREIGN KEY (id_service_contract) REFERENCES identitystore_service_contract (id_service_contract);
ALTER TABLE identitystore_service_contract_attribute_certification
    ADD CONSTRAINT fk_attribute_certification_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);
ALTER TABLE identitystore_service_contract_attribute_certification
    ADD CONSTRAINT fk_attribute_certification_certification_processus FOREIGN KEY (id_ref_attribute_certification_processus) REFERENCES identitystore_ref_certification_processus (id_ref_attribute_certification_processus);

--
-- Structure for table identitystore_attribute
--

CREATE TABLE identitystore_index_action
(
    id_index_action int AUTO_INCREMENT,
    customer_id     varchar(50) NOT NULL,
    action_type     varchar(50) NOT NULL,
    date_index      timestamp   NOT NULL,
    PRIMARY KEY (id_index_action)
);



DROP TABLE IF EXISTS identitystore_identity_search_rule;
CREATE TABLE identitystore_identity_search_rule (
    id_rule      int             AUTO_INCREMENT,
    type         varchar(8)      NOT NULL,
    PRIMARY KEY (id_rule)
);

DROP TABLE IF EXISTS identitystore_identity_search_rule_attribute;
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
-- Structure for table identitystore_quality_suspicious_identity
--


DROP TABLE IF EXISTS identitystore_quality_suspicious_identity;
CREATE TABLE identitystore_quality_suspicious_identity (
    id_suspicious_identity int AUTO_INCREMENT,
    customer_id varchar(50) default '' NOT NULL,
    id_duplicate_rule INT NOT NULL DEFAULT 0,
    date_create timestamp default CURRENT_TIMESTAMP not null,
    last_update_date timestamp,
    date_delete timestamp,
    is_deleted smallint default 0,
    PRIMARY KEY (id_suspicious_identity)
);


--
-- Structure for table identitystore_quality_suspicious_identity_excluded
--

DROP TABLE IF EXISTS identitystore_quality_suspicious_identity_excluded;
CREATE TABLE identitystore_quality_suspicious_identity_excluded (
    first_cuid varchar(50) default '' NOT NULL,
    second_cuid varchar(50) default '' NOT NULL
);

--
-- Structure for table identitystore_quality_suspicious_identity_lock
--
DROP TABLE IF EXISTS identitystore_quality_suspicious_identity_lock;
CREATE TABLE identitystore_quality_suspicious_identity_lock (
    customer_id varchar(50) default '' NOT NULL,
    date_lock_end timestamp,
    is_locked smallint default 0,
    author_type varchar(255),
    author_name varchar(255)
);

--
-- Structure for table identitystore_quality_suspicious_identity
--

DROP TABLE IF EXISTS identitystore_quality_suspicious_identity_excluded;
CREATE TABLE identitystore_quality_suspicious_identity_excluded
(
    first_customer_id  VARCHAR(50)  NOT NULL,
    second_customer_id VARCHAR(50)  NOT NULL,
    date_create        TIMESTAMP    NOT NULL,
    author_type        VARCHAR(255) NOT NULL,
    author_name        VARCHAR(255) NOT NULL
);
