-- Enhance queries
CREATE INDEX identitystore_identity_master_id ON identitystore_identity (id_master_identity);
CREATE INDEX identitystore_quality_suspicious_identity_cuid ON identitystore_quality_suspicious_identity (customer_id);
CREATE INDEX identitystore_identity_expiration_date ON identitystore_identity (expiration_date);
CREATE INDEX index_key_name ON identitystore_ref_attribute(key_name);
CREATE INDEX index_identity_attribute_certificate ON identitystore_identity_attribute_certificate(id_attribute_certificate);

-- Remove unused daemon
DELETE FROM core_datastore WHERE entity_key IN ('core.daemon.identityDuplicatesPurgeDaemon.interval', 'core.daemon.identityDuplicatesPurgeDaemon.onStartUp');

-- #365 - [API Contrat Service] - fournir libellés associés à des codes
DROP TABLE IF EXISTS identitystore_ref_attribute_values;
CREATE TABLE identitystore_ref_attribute_values
(
    id_attribute INTEGER,
    value        VARCHAR(50),
    label        VARCHAR(255)
);
ALTER TABLE identitystore_ref_attribute_values
    ADD CONSTRAINT fk_id_attribute FOREIGN KEY (id_attribute) REFERENCES identitystore_ref_attribute (id_attribute);

INSERT INTO identitystore_ref_attribute_values VALUES
                                                   (1, '0', 'Non défini'),
                                                   (1, '1', 'Femme'),
                                                   (1, '2', 'Homme');

-- renamed attribute key cache
DELETE FROM public.core_datastore WHERE entity_key = 'core.cache.status.identitystore.identityAttributeCache.enabled';
DELETE FROM public.core_datastore WHERE entity_key = 'core.cache.status.IdentityAttributeCache.enabled';