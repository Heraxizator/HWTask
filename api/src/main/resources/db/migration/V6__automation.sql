CREATE TABLE task_reminders (
    id UUID NOT NULL PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    remind_at TIMESTAMPTZ NOT NULL,
    fired_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_reminders_pending ON task_reminders (remind_at) WHERE fired_at IS NULL;

CREATE TABLE automation_rules (
    id UUID NOT NULL PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    trigger_type VARCHAR(64) NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_automation_rules_project ON automation_rules (project_id);
