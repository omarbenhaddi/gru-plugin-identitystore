--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'IDENTITYSTORE_LOCKS_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES
    ('IDENTITYSTORE_LOCKS_MANAGEMENT','identitystore.adminFeature.IdentityLocks.name',1,'jsp/admin/plugins/identitystore/IdentityLocks.jsp','identitystore.adminFeature.IdentityLocks.description',0,'identitystore',NULL,NULL,NULL,9);
