ALTER TABLE identitystore_identity_attribute ADD COLUMN lastupdate_application VARCHAR(100) NULL AFTER lastupdate_date;

ALTER TABLE identitystore_client_application ADD COLUMN is_application_authorized_to_delete_value INT(1) NOT NULL DEFAULT 0 AFTER code;

ALTER TABLE identitystore_identity_attribute_history  DROP COLUMN author_email;
ALTER TABLE identitystore_identity_attribute_history  CHANGE COLUMN author_service author_application VARCHAR(255) default '';