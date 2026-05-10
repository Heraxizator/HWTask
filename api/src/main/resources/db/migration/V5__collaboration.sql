CREATE TABLE task_comments (
    id UUID NOT NULL PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_task_comments_task ON task_comments (task_id);

CREATE TABLE task_attachments (
    id UUID NOT NULL PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    file_name VARCHAR(512) NOT NULL,
    content_type VARCHAR(255),
    size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_task_attachments_task ON task_attachments (task_id);

CREATE TABLE task_activity (
    id UUID NOT NULL PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users (id) ON DELETE SET NULL,
    event_type VARCHAR(64) NOT NULL,
    summary TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_task_activity_task ON task_activity (task_id, created_at DESC);
