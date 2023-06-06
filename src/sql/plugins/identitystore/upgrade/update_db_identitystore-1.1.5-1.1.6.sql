ALTER TABLE identitystore_identity_attribute_history  DROP COLUMN identity_name;

ALTER TABLE identitystore_identity_attribute ADD INDEX ix_attribute_value USING BTREE (attribute_value(50) ASC);