-- Run this to clean up manually created tables
-- Connect to your database first, then run:

DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS organizations CASCADE;

-- Flyway will recreate everything correctly
