CREATE TABLE task_checklist_items (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    title VARCHAR(512) NOT NULL,
    done BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_checklist_task ON task_checklist_items (task_id, sort_order);
