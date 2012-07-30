--
-- Structure for table searchstats_queries
--
DROP TABLE IF EXISTS searchstats_queries;
CREATE TABLE searchstats_queries (
  yyyy int DEFAULT NULL,
  mm int DEFAULT NULL,
  dd int DEFAULT NULL,
  hh int DEFAULT NULL,
  query varchar(255) DEFAULT NULL,
  results_count int DEFAULT NULL
);
