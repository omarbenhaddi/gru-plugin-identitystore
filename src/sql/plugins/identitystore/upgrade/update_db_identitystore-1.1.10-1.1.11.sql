ALTER TABLE identitystore_identity_attribute ADD COLUMN lastupdate_application VARCHAR(100) NULL AFTER lastupdate_date;

ALTER TABLE identitystore_client_application ADD COLUMN is_application_authorized_to_delete_value INT(1) NOT NULL DEFAULT 0 AFTER code;

ALTER TABLE identitystore_history_identity_attribute DROP COLUMN author_email;
ALTER TABLE identitystore_history_identity_attribute CHANGE COLUMN author_service author_application VARCHAR(255) default '';