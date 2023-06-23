DROP TABLE IF EXISTS identitystore_identity_history;

CREATE TABLE  identitystore_identity_history
(
    id_history            integer AUTO_INCREMENT,
    change_type           integer                                                                                           not null,
    change_satus          varchar(255)                                                                                      not null,
    change_message        varchar(255) default NULL::character varying,
    author_type           varchar(255)                                                                                      not null,
    author_name           varchar(255) default NULL::character varying,
    client_code           varchar(255) default NULL::character varying,
    id_identity           integer                                                                                           not null
    constraint fk_history_identity_id
    references identitystore_identity,
    modification_date     timestamp    default CURRENT_TIMESTAMP,
    PRIMARY KEY (id_history)
    );

