import { Clock, Trash2 } from 'lucide-react';

import type { ChecklistItemResponse } from '../../../api/checklist';
import type { TagResponse } from '../../../api/tags';
import type { TimeEntryResponse } from '../../../api/time';
import type {
  ActivityEntryResponse,
  CommentResponse,
  TaskTagResponse,
} from '../../../types/task';
import { formatDt, formatDurationSeconds } from '../taskDisplay';

export function TaskDetailPanel({
  tagsLoading,
  detailLoading,
  projectTags,
  detailTags,
  tagsMutationPending,
  onToggleDetailTag,
  timeLoading,
  timeEntries,
  activeTimerEntry,
  startTimePending,
  stopTimePending,
  onStartTime,
  onStopTime,
  checklistLoading,
  checklistItems,
  checklistNewTitle,
  onChecklistTitleChange,
  onAddChecklist,
  checklistAddPending,
  toggleCheckPending,
  deleteCheckPending,
  onToggleChecklistItem,
  onDeleteChecklistItem,
  commentsLoading,
  comments,
  commentText,
  onCommentChange,
  commentPending,
  onSubmitComment,
  activityLoading,
  activity,
  mutePending,
  unmutePending,
  onMute,
  onUnmute,
}: {
  tagsLoading: boolean;
  detailLoading: boolean;
  projectTags: TagResponse[];
  detailTags: TaskTagResponse[];
  tagsMutationPending: boolean;
  onToggleDetailTag: (tagId: string, checked: boolean) => void;
  timeLoading: boolean;
  timeEntries: TimeEntryResponse[];
  activeTimerEntry: TimeEntryResponse | null;
  startTimePending: boolean;
  stopTimePending: boolean;
  onStartTime: () => void;
  onStopTime: () => void;
  checklistLoading: boolean;
  checklistItems: ChecklistItemResponse[];
  checklistNewTitle: string;
  onChecklistTitleChange: (v: string) => void;
  onAddChecklist: () => void;
  checklistAddPending: boolean;
  toggleCheckPending: boolean;
  deleteCheckPending: boolean;
  onToggleChecklistItem: (id: string, done: boolean) => void;
  onDeleteChecklistItem: (id: string) => void;
  commentsLoading: boolean;
  comments: CommentResponse[];
  commentText: string;
  onCommentChange: (v: string) => void;
  commentPending: boolean;
  onSubmitComment: () => void;
  activityLoading: boolean;
  activity: ActivityEntryResponse[];
  mutePending: boolean;
  unmutePending: boolean;
  onMute: () => void;
  onUnmute: () => void;
}) {
  return (
    <section className="panel" style={{ marginTop: '1rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1rem', flexWrap: 'wrap' }}>
        <h2 style={{ marginTop: 0 }}>Карточка задачи</h2>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          <button type="button" className="btn btn-ghost" disabled={mutePending} onClick={onMute}>
            Не уведомлять об этой задаче
          </button>
          <button type="button" className="btn btn-ghost" disabled={unmutePending} onClick={onUnmute}>
            Включить уведомления
          </button>
        </div>
      </div>

      <div style={{ marginBottom: '1.25rem' }}>
        <h3 className="muted" style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>
          Теги
        </h3>
        {tagsLoading || detailLoading ? (
          <p className="muted">Загрузка…</p>
        ) : projectTags.length === 0 ? (
          <p className="muted" style={{ margin: 0 }}>В проекте нет тегов — создайте в форме задачи.</p>
        ) : (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
            {projectTags.map((tag) => (
              <label
                key={tag.id}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.35rem',
                  fontSize: '0.9rem',
                  cursor: 'pointer',
                  userSelect: 'none',
                }}
              >
                <input
                  type="checkbox"
                  checked={detailTags.some((t) => t.id === tag.id)}
                  disabled={tagsMutationPending}
                  onChange={(e) => onToggleDetailTag(tag.id, e.target.checked)}
                />
                {tag.name}
              </label>
            ))}
          </div>
        )}
      </div>

      <div
        style={{
          display: 'grid',
          gap: '1.5rem',
          gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
          marginBottom: '1.5rem',
        }}
      >
        <div>
          <h3 className="muted" style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>
            Учёт времени
          </h3>
          {timeLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <>
              {activeTimerEntry && (
                <p style={{ marginTop: 0, fontSize: '0.9rem' }}>
                  <Clock size={14} style={{ verticalAlign: 'middle', marginRight: '0.25rem' }} aria-hidden />
                  Идёт учёт с {formatDt(activeTimerEntry.startedAt)}
                </p>
              )}
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '0.75rem' }}>
                <button
                  type="button"
                  className="btn btn-primary"
                  disabled={startTimePending || !!activeTimerEntry}
                  onClick={onStartTime}
                >
                  Старт
                </button>
                <button
                  type="button"
                  className="btn btn-ghost"
                  disabled={stopTimePending || !activeTimerEntry}
                  onClick={onStopTime}
                >
                  Стоп
                </button>
              </div>
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, maxHeight: 200, overflow: 'auto' }}>
                {timeEntries.length === 0 ? (
                  <li className="muted" style={{ fontSize: '0.9rem' }}>Записей пока нет</li>
                ) : (
                  timeEntries.map((te) => (
                    <li
                      key={te.id}
                      style={{
                        fontSize: '0.85rem',
                        marginBottom: '0.4rem',
                        paddingBottom: '0.4rem',
                        borderBottom: '1px solid var(--color-border)',
                      }}
                    >
                      <span className="muted">{formatDt(te.startedAt)}</span>
                      {' — '}
                      {te.endedAt ? (
                        <>
                          {formatDt(te.endedAt)}
                          <span className="muted"> ({formatDurationSeconds(te.durationSeconds)})</span>
                        </>
                      ) : (
                        <span className="badge badge-progress" style={{ fontSize: '0.7rem' }}>активна</span>
                      )}
                    </li>
                  ))
                )}
              </ul>
            </>
          )}
        </div>
        <div>
          <h3 className="muted" style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>
            Чеклист
          </h3>
          {checklistLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <>
              <ul style={{ listStyle: 'none', padding: 0, margin: '0 0 0.75rem 0' }}>
                {checklistItems.map((item) => (
                  <li
                    key={item.id}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      marginBottom: '0.35rem',
                      fontSize: '0.9rem',
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={item.done}
                      disabled={toggleCheckPending}
                      onChange={(e) => onToggleChecklistItem(item.id, e.target.checked)}
                    />
                    <span style={{ flex: 1, textDecoration: item.done ? 'line-through' : undefined, opacity: item.done ? 0.75 : 1 }}>
                      {item.title}
                    </span>
                    <button
                      type="button"
                      className="btn btn-ghost"
                      style={{ padding: '0.15rem 0.35rem' }}
                      aria-label={`Удалить пункт: ${item.title}`}
                      disabled={deleteCheckPending}
                      onClick={() => onDeleteChecklistItem(item.id)}
                    >
                      <Trash2 size={14} aria-hidden />
                    </button>
                  </li>
                ))}
              </ul>
              <div className="field" style={{ marginBottom: 0 }}>
                <input
                  type="text"
                  value={checklistNewTitle}
                  onChange={(e) => onChecklistTitleChange(e.target.value)}
                  placeholder="Новый пункт…"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      onAddChecklist();
                    }
                  }}
                />
                <button
                  type="button"
                  className="btn btn-primary"
                  style={{ marginTop: '0.5rem' }}
                  disabled={!checklistNewTitle.trim() || checklistAddPending}
                  onClick={onAddChecklist}
                >
                  Добавить
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      <div style={{ display: 'grid', gap: '1.5rem', gridTemplateColumns: '1fr 1fr' }}>
        <div>
          <h3 className="muted" style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>
            Комментарии
          </h3>
          {commentsLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
              {comments.map((c) => (
                <li key={c.id} style={{ marginBottom: '0.75rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--color-border)' }}>
                  <div className="muted" style={{ fontSize: '0.8rem' }}>
                    {formatDt(c.createdAt)}
                  </div>
                  <div>{c.body}</div>
                </li>
              ))}
            </ul>
          )}
          <div className="field" style={{ marginTop: '0.75rem' }}>
            <textarea value={commentText} onChange={(e) => onCommentChange(e.target.value)} rows={2} placeholder="Комментарий…" />
            <button type="button" className="btn btn-primary" style={{ marginTop: '0.5rem' }} disabled={!commentText.trim() || commentPending} onClick={onSubmitComment}>
              Отправить
            </button>
          </div>
        </div>
        <div>
          <h3 className="muted" style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>
            Активность
          </h3>
          {activityLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, maxHeight: 280, overflow: 'auto' }}>
              {activity.map((a) => (
                <li key={a.id} style={{ marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                  <span className="muted">{formatDt(a.createdAt)}</span> — {a.summary}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </section>
  );
}
