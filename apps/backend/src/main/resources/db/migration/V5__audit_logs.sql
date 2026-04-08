CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_username VARCHAR(100),
    actor_role VARCHAR(20),
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id BIGINT,
    trace_id VARCHAR(120),
    metadata_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);
CREATE INDEX idx_audit_logs_entity_type_entity_id ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor_username ON audit_logs (actor_username);
