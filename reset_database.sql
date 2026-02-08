-- Run this in pgAdmin to clean up and let Flyway handle everything

DROP TABLE IF EXISTS ticket_messages CASCADE;
DROP TABLE IF EXISTS sla_config CASCADE;
DROP TABLE IF EXISTS tickets CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS organizations CASCADE;
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- After running this, restart your Spring Boot application
-- Flyway will automatically create all tables correctly
