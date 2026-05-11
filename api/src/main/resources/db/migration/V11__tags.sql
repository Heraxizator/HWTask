CREATE TABLE tags (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    UNIQUE (project_id, name)
);

CREATE INDEX idx_tags_project ON tags (project_id);

CREATE TABLE task_tags (
    task_id UUID NOT NULL REFERENCES tasks (id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags (id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, tag_id)
);

CREATE INDEX idx_task_tags_tag ON task_tags (tag_id);
