--
-- Structure for table identitystore_attribute_certificate
--
ALTER TABLE identitystore_attribute_certificate DROP COLUMN certificate_level;

--
-- Structure for table identitystore_attribute
--
ALTER TABLE identitystore_attribute ADD COLUMN common_search_key VARCHAR(100) NULL;
UPDATE identitystore_attribute SET common_search_key = 'common_lastname' WHERE key_name IN ('family_name', 'preferred_username');
UPDATE identitystore_attribute SET common_search_key = 'common_email' WHERE key_name IN ('email', 'login');
UPDATE identitystore_attribute SET common_search_key = 'common_phone' WHERE key_name IN ('mobile_phone', 'fixed_phone');