--
-- Dumping data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'SEARCH_STATS_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('SEARCH_STATS_MANAGEMENT','searchstats.adminFeature.searchstats_management.name',1,'jsp/admin/plugins/searchstats/ManageSearchStats.jsp','searchstats.adminFeature.searchstats_management.description',0,'searchstats',NULL,NULL,NULL,4);


--
-- Dumping data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'SEARCH_STATS_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('SEARCH_STATS_MANAGEMENT',1);
