ALTER TABLE tasks
    ADD COLUMN deleted_at TIMESTAMPTZ;

CREATE INDEX idx_tasks_active_project ON tasks (project_id) WHERE deleted_at IS NULL;
