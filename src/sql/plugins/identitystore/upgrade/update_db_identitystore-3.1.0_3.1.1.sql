-- Enhance queries
CREATE INDEX identitystore_identity_master_id ON identitystore_identity (id_master_identity);
CREATE INDEX identitystore_quality_suspicious_identity_cuid ON identitystore_quality_suspicious_identity (customer_id);
CREATE INDEX identitystore_identity_expiration_date ON identitystore_identity (expiration_date);
CREATE INDEX index_key_name ON identitystore_ref_attribute(key_name);
CREATE INDEX index_identity_attribute_certificate ON identitystore_identity_attribute_certificate(id_attribute_certificate);

-- Remove unused daemon
DELETE FROM core_datastore WHERE entity_key IN ('core.daemon.identityDuplicatesPurgeDaemon.interval', 'core.daemon.identityDuplicatesPurgeDaemon.onStartUp');