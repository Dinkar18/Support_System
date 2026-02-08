-- Complete database reset
-- Run this in pgAdmin Query Tool

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- After running this, restart Spring Boot
-- Flyway will create everything from scratch
