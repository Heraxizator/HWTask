import { Search, Tag } from 'lucide-react';

import type { TagResponse } from '../../../api/tags';

export function TasksToolbar({
  searchInput,
  onSearchChange,
  tags,
  filterTagIds,
  onToggleFilterTag,
}: {
  searchInput: string;
  onSearchChange: (value: string) => void;
  tags: TagResponse[];
  filterTagIds: string[];
  onToggleFilterTag: (tagId: string) => void;
}) {
  return (
    <div className="tasks-toolbar">
      <div className="field tasks-toolbar__search">
        <label htmlFor="task-search" className="toolbar-label">
          Поиск
        </label>
        <div className="search-shell">
          <Search size={16} aria-hidden />
          <input
            id="task-search"
            type="search"
            value={searchInput}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Название или описание…"
            autoComplete="off"
            className="portal-field__input portal-field__input--sm tasks-toolbar__search-input"
          />
        </div>
      </div>
      <div className="tasks-toolbar__tags">
        <span className="toolbar-label">Теги</span>
        <div className="tag-filter-row">
          {tags.length === 0 ? (
            <span className="muted" style={{ fontSize: '0.9rem' }}>
              Нет тегов в проекте
            </span>
          ) : (
            tags.map((tag) => (
              <label key={tag.id} className="tag-filter-pill">
                <input
                  type="checkbox"
                  checked={filterTagIds.includes(tag.id)}
                  onChange={() => onToggleFilterTag(tag.id)}
                />
                <Tag size={14} aria-hidden strokeWidth={2} />
                {tag.name}
              </label>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
