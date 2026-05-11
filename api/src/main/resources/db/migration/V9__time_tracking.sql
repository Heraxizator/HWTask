CREATE TABLE task_time_entries (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    comment_note VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_time_entries_task ON task_time_entries (task_id);
CREATE INDEX idx_time_entries_user_open ON task_time_entries (user_id) WHERE ended_at IS NULL;
