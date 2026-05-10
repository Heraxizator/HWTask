ALTER TABLE tasks
    ADD COLUMN project_id UUID REFERENCES projects (id) ON DELETE CASCADE,
    ADD COLUMN parent_task_id UUID REFERENCES tasks (id) ON DELETE CASCADE,
    ADD COLUMN assignee_id UUID REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN created_by UUID REFERENCES users (id) ON DELETE SET NULL,
    ADD COLUMN due_at TIMESTAMPTZ;

DELETE FROM tasks;

ALTER TABLE tasks ALTER COLUMN project_id SET NOT NULL;

CREATE INDEX idx_tasks_project ON tasks (project_id);
CREATE INDEX idx_tasks_parent ON tasks (parent_task_id);
CREATE INDEX idx_tasks_assignee ON tasks (assignee_id);
CREATE INDEX idx_tasks_due ON tasks (due_at);

CREATE TABLE task_members (
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (task_id, user_id, role)
);

CREATE INDEX idx_task_members_user ON task_members (user_id);
