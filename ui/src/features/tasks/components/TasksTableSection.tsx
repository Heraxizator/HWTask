import { ClipboardList, Loader2, MessageSquare, Pencil, Trash2 } from 'lucide-react';

import type { TagResponse } from '../../../api/tags';
import {
  Badge,
  BadgeColor,
  BadgeSize,
  BadgeType,
  Button,
  ButtonColors,
  ButtonSizes,
  ButtonVariants,
} from '../../../portal-ui';
import type { TaskResponse, TaskStatus } from '../../../types/task';
import { formatDt, statusLabel } from '../taskDisplay';

function badgeColorForStatus(s: TaskStatus): (typeof BadgeColor)[keyof typeof BadgeColor] {
  if (s === 'DONE') return BadgeColor.SUCCESS;
  if (s === 'IN_PROGRESS') return BadgeColor.PRIMARY;
  return BadgeColor.NEUTRAL;
}
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
        <div className="state-block state-block--rich">
          <div className="state-block__icon" aria-hidden>
            <ClipboardList size={26} strokeWidth={1.75} />
          </div>
          <p className="state-block__title">Выберите проект</p>
          <p className="state-block__lead">Создайте организацию и проект через API или выберите доступный проект выше.</p>
        </div>
      ) : loadingList ? (
        <div className="state-block state-block--rich" aria-live="polite">
          <div className="state-block__icon" aria-hidden>
            <Loader2 size={28} className="spin" strokeWidth={2} />
          </div>
          <p className="state-block__title">Загрузка задач…</p>
          <p className="state-block__lead muted">Подождите, получаем список для выбранного проекта.</p>
        </div>
      ) : tasks.length === 0 && !listErr ? (
        <div className="state-block state-block--rich">
          <div className="state-block__icon" aria-hidden>
            <ClipboardList size={26} strokeWidth={1.75} />
          </div>
          <p className="state-block__title">Пока тихо</p>
          <p className="state-block__lead">В этом проекте ещё нет задач. Нажмите «Новая задача», чтобы начать работу.</p>
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
                  <th className="tasks-table__actions" aria-label="Действия" />
                </tr>
              </thead>
              <tbody>
                {tasks.map((t: TaskResponse) => (
                  <tr key={t.id} className={detailId === t.id ? 'is-row-active' : undefined}>
                    <td>
                      <Button
                        type="button"
                        variant={ButtonVariants.GHOST}
                        color={ButtonColors.NEUTRAL}
                        size={ButtonSizes.MEDIUM}
                        className="task-title-btn"
                        onClick={() => onToggleDetail(t.id)}
                      >
                        {t.title}
                        {detailId === t.id && (
                          <MessageSquare size={14} style={{ marginLeft: '0.35rem', opacity: 0.72 }} aria-hidden />
                        )}
                      </Button>
                      {t.description && (
                        <div className="muted task-desc-snippet">
                          {t.description.length > 120 ? `${t.description.slice(0, 117)}…` : t.description}
                        </div>
                      )}
                      {(t.tags?.length ?? 0) > 0 && (
                        <div className="chip-tags">
                          {(t.tags ?? []).map((tg) => (
                            <span key={tg.id} className="chip-tag">
                              {tg.name}
                            </span>
                          ))}
                        </div>
                      )}
                    </td>
                    <td>
                      <Badge
                        type={BadgeType.SOFT}
                        color={badgeColorForStatus(t.status)}
                        size={BadgeSize.SMALL}
                        text={statusLabel(t.status)}
                      />
                    </td>
                    <td className="muted">{t.dueAt ? formatDt(t.dueAt) : '—'}</td>
                    <td className="muted">{formatDt(t.updatedAt)}</td>
                    <td className="tasks-table__actions">
                      <div className="row-actions">
                        <Button
                          type="button"
                          variant={ButtonVariants.GHOST}
                          color={ButtonColors.NEUTRAL}
                          size={ButtonSizes.MEDIUM}
                          iconOnly
                          className="btn-icon"
                          onClick={() => void onEdit(t.id)}
                          aria-label={`Редактировать: ${t.title}`}
                        >
                          <Pencil size={16} aria-hidden />
                        </Button>
                        <Button
                          type="button"
                          variant={ButtonVariants.FILLED}
                          color={ButtonColors.DANGER}
                          size={ButtonSizes.MEDIUM}
                          iconOnly
                          className="btn-icon"
                          onClick={() => onRequestDelete(t.id)}
                          aria-label={`Удалить: ${t.title}`}
                        >
                          <Trash2 size={16} aria-hidden />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <footer className="pagination">
            <span className="pagination-meta">
              Страница <strong>{listNumber + 1}</strong> из <strong>{Math.max(totalPages, 1)}</strong>
              {totalElements != null && (
                <>
                  {' '}
                  · <span className="muted">записей: {totalElements}</span>
                </>
              )}
            </span>
            <div className="pagination-controls">
              <Button
                type="button"
                variant={ButtonVariants.GHOST}
                color={ButtonColors.NEUTRAL}
                size={ButtonSizes.SMALL}
                disabled={page <= 0}
                onClick={onPagePrev}
              >
                Назад
              </Button>
              <Button
                type="button"
                variant={ButtonVariants.GHOST}
                color={ButtonColors.NEUTRAL}
                size={ButtonSizes.SMALL}
                disabled={totalPages === 0 || page >= totalPages - 1}
                onClick={onPageNext}
              >
                Вперёд
              </Button>
            </div>
          </footer>
        </>
      )}
    </section>
  );
}
