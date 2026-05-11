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
    <div
      style={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: '0.75rem',
        alignItems: 'flex-end',
        marginBottom: '1rem',
      }}
    >
      <div className="field" style={{ flex: '1 1 220px', marginBottom: 0 }}>
        <label htmlFor="task-search" className="muted" style={{ fontSize: '0.85rem' }}>
          Поиск
        </label>
        <div style={{ position: 'relative' }}>
          <Search
            size={16}
            style={{
              position: 'absolute',
              left: '0.65rem',
              top: '50%',
              transform: 'translateY(-50%)',
              opacity: 0.45,
              pointerEvents: 'none',
            }}
            aria-hidden
          />
          <input
            id="task-search"
            type="search"
            value={searchInput}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Название или описание…"
            autoComplete="off"
            style={{ paddingLeft: '2.25rem', width: '100%' }}
          />
        </div>
      </div>
      <div style={{ flex: '2 1 280px' }}>
        <span className="muted" style={{ fontSize: '0.85rem', display: 'block', marginBottom: '0.35rem' }}>
          Теги
        </span>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
          {tags.length === 0 ? (
            <span className="muted" style={{ fontSize: '0.9rem' }}>Нет тегов в проекте</span>
          ) : (
            tags.map((tag) => (
              <label
                key={tag.id}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.35rem',
                  fontSize: '0.85rem',
                  cursor: 'pointer',
                  userSelect: 'none',
                }}
              >
                <input
                  type="checkbox"
                  checked={filterTagIds.includes(tag.id)}
                  onChange={() => onToggleFilterTag(tag.id)}
                />
                <Tag size={14} aria-hidden />
                {tag.name}
              </label>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
