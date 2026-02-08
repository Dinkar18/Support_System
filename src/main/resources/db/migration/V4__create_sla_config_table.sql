CREATE TABLE sla_config (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL UNIQUE REFERENCES tickets(id) ON DELETE CASCADE,
    first_response_deadline TIMESTAMP,
    resolution_deadline TIMESTAMP,
    first_response_met BOOLEAN DEFAULT false,
    resolution_met BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sla_ticket ON sla_config(ticket_id);
