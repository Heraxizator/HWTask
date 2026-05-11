import { Loader2, MessageSquare, Pencil, Trash2 } from 'lucide-react';

import type { TagResponse } from '../../../api/tags';
import type { TaskResponse } from '../../../types/task';
import { formatDt, statusBadgeClass, statusLabel } from '../taskDisplay';
import { TasksToolbar } from './TasksToolbar';

export function TasksTableSection({
  projectId,
  loadingList,
  listErr,
  tasks,
  searchInput,
  onSearchChange,
  projectTags,
  filterTagIds,
  onToggleFilterTag,
  detailId,
  onToggleDetail,
  onEdit,
  onRequestDelete,
  page,
  totalPages,
  listNumber,
  totalElements,
  onPagePrev,
  onPageNext,
}: {
  projectId: string | null;
  loadingList: boolean;
  listErr: string | null;
  tasks: TaskResponse[];
  searchInput: string;
  onSearchChange: (value: string) => void;
  projectTags: TagResponse[];
  filterTagIds: string[];
  onToggleFilterTag: (tagId: string) => void;
  detailId: string | null;
  onToggleDetail: (taskId: string) => void;
  onEdit: (taskId: string) => void;
  onRequestDelete: (taskId: string) => void;
  page: number;
  totalPages: number;
  listNumber: number;
  totalElements: number | undefined;
  onPagePrev: () => void;
  onPageNext: () => void;
}) {
  return (
    <section className="panel">
      {!projectId ? (
        <div className="state-block">Выберите или создайте проект (организация → проект).</div>
      ) : loadingList ? (
        <div className="state-block" aria-live="polite">
          <Loader2 size={28} className="spin" style={{ animation: 'spin 0.9s linear infinite', color: 'var(--color-accent)', marginBottom: '0.75rem' }} aria-hidden />
          <strong>Загрузка…</strong>
        </div>
      ) : tasks.length === 0 && !listErr ? (
        <div className="state-block">
          <p style={{ margin: 0 }}>В проекте пока нет задач.</p>
        </div>
      ) : (
        <>
          <TasksToolbar
            searchInput={searchInput}
            onSearchChange={onSearchChange}
            tags={projectTags}
            filterTagIds={filterTagIds}
            onToggleFilterTag={onToggleFilterTag}
          />
          <div className="table-wrap">
            <table className="tasks-table">
              <thead>
                <tr>
                  <th>Задача</th>
                  <th>Статус</th>
                  <th>Срок (UTC)</th>
                  <th>Обновлено</th>
                  <th style={{ width: '1%', textAlign: 'right' }} aria-label="Действия" />
                </tr>
              </thead>
              <tbody>
                {tasks.map((t: TaskResponse) => (
                  <tr key={t.id}>
                    <td>
                      <button
                        type="button"
                        className="btn btn-ghost"
                        style={{ fontWeight: 600, textAlign: 'left', padding: 0 }}
                        onClick={() => onToggleDetail(t.id)}
                      >
                        {t.title}
                        {detailId === t.id && (
                          <MessageSquare size={14} style={{ marginLeft: '0.35rem', opacity: 0.7 }} aria-hidden />
                        )}
                      </button>
                      {t.description && (
                        <div className="muted" style={{ marginTop: '0.2rem' }}>
                          {t.description.length > 120 ? `${t.description.slice(0, 117)}…` : t.description}
                        </div>
                      )}
                      {(t.tags?.length ?? 0) > 0 && (
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.25rem', marginTop: '0.35rem' }}>
                          {(t.tags ?? []).map((tg) => (
                            <span
                              key={tg.id}
                              className="badge"
                              style={{ fontSize: '0.7rem', fontWeight: 500 }}
                            >
                              {tg.name}
                            </span>
                          ))}
                        </div>
                      )}
                    </td>
                    <td>
                      <span className={statusBadgeClass(t.status)}>{statusLabel(t.status)}</span>
                    </td>
                    <td className="muted">{t.dueAt ? formatDt(t.dueAt) : '—'}</td>
                    <td className="muted">{formatDt(t.updatedAt)}</td>
                    <td>
                      <div className="row-actions">
                        <button type="button" className="btn btn-ghost" onClick={() => void onEdit(t.id)} aria-label={`Редактировать: ${t.title}`}>
                          <Pencil size={16} aria-hidden />
                        </button>
                        <button type="button" className="btn btn-danger" onClick={() => onRequestDelete(t.id)} aria-label={`Удалить: ${t.title}`}>
                          <Trash2 size={16} aria-hidden />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <footer className="pagination">
            <span>
              Страница {listNumber + 1} из {Math.max(totalPages, 1)}
              {totalElements != null && ` · всего ${totalElements}`}
            </span>
            <div className="pagination-controls">
              <button type="button" className="btn btn-ghost" disabled={page <= 0} onClick={onPagePrev}>
                Назад
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                disabled={totalPages === 0 || page >= totalPages - 1}
                onClick={onPageNext}
              >
                Вперёд
              </button>
            </div>
          </footer>
        </>
      )}
    </section>
  );
}
