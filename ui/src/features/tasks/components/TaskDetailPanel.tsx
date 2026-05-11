import { Clock, Trash2 } from 'lucide-react';

import type { ChecklistItemResponse } from '../../../api/checklist';
import {
  Button,
  ButtonColors,
  ButtonSizes,
  ButtonVariants,
} from '../../../portal-ui';
import type { TagResponse } from '../../../api/tags';
import type { TimeEntryResponse } from '../../../api/time';
import type {
  ActivityEntryResponse,
  AttachmentResponse,
  CommentResponse,
  ReminderResponse,
  TaskResponse,
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
  subtasksLoading,
  subtasks,
  onOpenSubtask,
  onAddSubtask,
  attachmentsLoading,
  attachments,
  uploadAttachmentPending,
  onUploadAttachmentFiles,
  onDownloadAttachment,
  remindersLoading,
  reminders,
  reminderLocal,
  onReminderLocalChange,
  onAddReminder,
  reminderPending,
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
  subtasksLoading: boolean;
  subtasks: TaskResponse[];
  onOpenSubtask: (taskId: string) => void;
  onAddSubtask: () => void;
  attachmentsLoading: boolean;
  attachments: AttachmentResponse[];
  uploadAttachmentPending: boolean;
  onUploadAttachmentFiles: (files: FileList | null) => void;
  onDownloadAttachment: (attachmentId: string, fileName: string) => void;
  remindersLoading: boolean;
  reminders: ReminderResponse[];
  reminderLocal: string;
  onReminderLocalChange: (v: string) => void;
  onAddReminder: () => void;
  reminderPending: boolean;
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
    <section className="panel detail-panel">
      <div className="detail-panel__head">
        <h2 className="detail-panel__title">Карточка задачи</h2>
        <div className="detail-panel__actions">
          <Button
            type="button"
            variant={ButtonVariants.SOFT}
            color={ButtonColors.PRIMARY}
            size={ButtonSizes.MEDIUM}
            disabled={mutePending}
            onClick={onMute}
          >
            Не уведомлять
          </Button>
          <Button
            type="button"
            variant={ButtonVariants.SOFT}
            color={ButtonColors.PRIMARY}
            size={ButtonSizes.MEDIUM}
            disabled={unmutePending}
            onClick={onUnmute}
          >
            Включить уведомления
          </Button>
        </div>
      </div>

      <div className="detail-section-block">
        <h3 className="subsection-head">Теги</h3>
        {tagsLoading || detailLoading ? (
          <p className="muted">Загрузка…</p>
        ) : projectTags.length === 0 ? (
          <p className="muted" style={{ margin: 0 }}>
            В проекте нет тегов — добавьте их в форме создания или редактирования задачи.
          </p>
        ) : (
          <div className="tag-assign-row">
            {projectTags.map((tag) => (
              <label key={tag.id} className="tag-assign-pill">
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

      <div className="detail-grid-2">
        <div className="detail-well">
          <h3 className="subsection-head">Учёт времени</h3>
          {timeLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <>
              {activeTimerEntry && (
                <div className="time-active-banner">
                  <Clock size={15} strokeWidth={2} aria-hidden />
                  <span>
                    Идёт учёт с <strong>{formatDt(activeTimerEntry.startedAt)}</strong>
                  </span>
                </div>
              )}
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '0.85rem' }}>
                <Button
                  type="button"
                  variant={ButtonVariants.FILLED}
                  color={ButtonColors.PRIMARY}
                  size={ButtonSizes.SMALL}
                  loading={startTimePending}
                  disabled={!!activeTimerEntry}
                  onClick={onStartTime}
                >
                  Старт
                </Button>
                <Button
                  type="button"
                  variant={ButtonVariants.GHOST}
                  color={ButtonColors.NEUTRAL}
                  size={ButtonSizes.SMALL}
                  loading={stopTimePending}
                  disabled={!activeTimerEntry}
                  onClick={onStopTime}
                >
                  Стоп
                </Button>
              </div>
              <ul className="time-entry-list">
                {timeEntries.length === 0 ? (
                  <li className="muted" style={{ fontSize: '0.9rem' }}>
                    Записей пока нет
                  </li>
                ) : (
                  timeEntries.map((te) => (
                    <li key={te.id} className="time-entry-row muted">
                      <span>{formatDt(te.startedAt)}</span>
                      {' — '}
                      {te.endedAt ? (
                        <>
                          {formatDt(te.endedAt)}
                          <span className="muted"> ({formatDurationSeconds(te.durationSeconds)})</span>
                        </>
                      ) : (
                        <span className="badge badge-progress" style={{ fontSize: '0.65rem', marginLeft: '0.25rem' }}>
                          активна
                        </span>
                      )}
                    </li>
                  ))
                )}
              </ul>
            </>
          )}
        </div>
        <div className="detail-well">
          <h3 className="subsection-head">Чеклист</h3>
          {checklistLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <>
              <ul className="checklist-list">
                {checklistItems.map((item) => (
                  <li key={item.id} className="checklist-row">
                    <input
                      type="checkbox"
                      checked={item.done}
                      disabled={toggleCheckPending}
                      onChange={(e) => onToggleChecklistItem(item.id, e.target.checked)}
                    />
                    <span className={item.done ? 'checklist-done' : undefined}>{item.title}</span>
                    <Button
                      type="button"
                      variant={ButtonVariants.GHOST}
                      color={ButtonColors.NEUTRAL}
                      size={ButtonSizes.SMALL}
                      iconOnly
                      className="btn-icon checklist-delete-btn"
                      style={{ marginLeft: 'auto' }}
                      aria-label={`Удалить пункт: ${item.title}`}
                      disabled={deleteCheckPending}
                      onClick={() => onDeleteChecklistItem(item.id)}
                    >
                      <Trash2 size={14} aria-hidden />
                    </Button>
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
                <Button
                  type="button"
                  variant={ButtonVariants.FILLED}
                  color={ButtonColors.PRIMARY}
                  size={ButtonSizes.SMALL}
                  style={{ marginTop: '0.55rem' }}
                  loading={checklistAddPending}
                  disabled={!checklistNewTitle.trim()}
                  onClick={onAddChecklist}
                >
                  Добавить пункт
                </Button>
              </div>
            </>
          )}
        </div>
      </div>

      <div className="detail-section-block">
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '0.75rem', flexWrap: 'wrap' }}>
          <h3 className="subsection-head" style={{ margin: 0 }}>
            Подзадачи
          </h3>
          <Button
            type="button"
            variant={ButtonVariants.FILLED}
            color={ButtonColors.PRIMARY}
            size={ButtonSizes.SMALL}
            onClick={onAddSubtask}
          >
            Добавить подзадачу
          </Button>
        </div>
        {subtasksLoading ? (
          <p className="muted">Загрузка…</p>
        ) : subtasks.length === 0 ? (
          <p className="muted" style={{ margin: 0 }}>
            Пока нет подзадач.
          </p>
        ) : (
          <ul style={{ margin: '0.5rem 0 0', paddingLeft: '1.1rem' }}>
            {subtasks.map((s) => (
              <li key={s.id} style={{ marginBottom: '0.35rem' }}>
                <Button
                  type="button"
                  variant={ButtonVariants.GHOST}
                  color={ButtonColors.NEUTRAL}
                  size={ButtonSizes.SMALL}
                  className="subtask-link-btn"
                  onClick={() => onOpenSubtask(s.id)}
                >
                  {s.title}
                </Button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="detail-grid-2">
        <div className="detail-well">
          <h3 className="subsection-head">Вложения</h3>
          {attachmentsLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <>
              <ul className="checklist-list" style={{ marginBottom: '0.65rem' }}>
                {attachments.length === 0 ? (
                  <li className="muted" style={{ fontSize: '0.9rem' }}>
                    Файлов нет
                  </li>
                ) : (
                  attachments.map((a) => (
                    <li key={a.id} className="checklist-row" style={{ alignItems: 'center' }}>
                      <span style={{ flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {a.fileName}{' '}
                        <span className="muted" style={{ fontSize: '0.78rem' }}>
                          ({Math.round(a.sizeBytes / 1024)} КиБ)
                        </span>
                      </span>
                      <Button
                        type="button"
                        variant={ButtonVariants.SOFT}
                        color={ButtonColors.PRIMARY}
                        size={ButtonSizes.SMALL}
                        onClick={() => onDownloadAttachment(a.id, a.fileName)}
                      >
                        Скачать
                      </Button>
                    </li>
                  ))
                )}
              </ul>
              <input
                type="file"
                disabled={uploadAttachmentPending}
                onChange={(e) => {
                  onUploadAttachmentFiles(e.target.files);
                  e.target.value = '';
                }}
              />
            </>
          )}
        </div>
        <div className="detail-well">
          <h3 className="subsection-head">Напоминания</h3>
          {remindersLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <>
              <ul className="time-entry-list" style={{ marginBottom: '0.65rem' }}>
                {reminders.length === 0 ? (
                  <li className="muted" style={{ fontSize: '0.9rem' }}>
                    Нет напоминаний
                  </li>
                ) : (
                  reminders.map((r) => (
                    <li key={r.id} className="time-entry-row muted">
                      {formatDt(r.remindAt)}
                      {r.firedAt ? (
                        <span className="muted"> (отправлено)</span>
                      ) : null}
                    </li>
                  ))
                )}
              </ul>
              <div className="field" style={{ marginBottom: 0 }}>
                <label htmlFor="reminder-local">Когда напомнить</label>
                <input
                  id="reminder-local"
                  type="datetime-local"
                  value={reminderLocal}
                  onChange={(e) => onReminderLocalChange(e.target.value)}
                />
                <Button
                  type="button"
                  variant={ButtonVariants.FILLED}
                  color={ButtonColors.PRIMARY}
                  size={ButtonSizes.SMALL}
                  style={{ marginTop: '0.55rem' }}
                  loading={reminderPending}
                  disabled={!reminderLocal}
                  onClick={onAddReminder}
                >
                  Запланировать
                </Button>
              </div>
            </>
          )}
        </div>
      </div>

      <div className="detail-split">
        <div>
          <h3 className="subsection-head">Комментарии</h3>
          {commentsLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <ul className="comment-list">
              {comments.map((c) => (
                <li key={c.id} className="comment-item">
                  <div className="muted" style={{ fontSize: '0.78rem', marginBottom: '0.25rem' }}>
                    {formatDt(c.createdAt)}
                  </div>
                  <div style={{ lineHeight: 1.48 }}>{c.body}</div>
                </li>
              ))}
            </ul>
          )}
          <div className="field" style={{ marginTop: '0.85rem' }}>
            <textarea
              value={commentText}
              onChange={(e) => onCommentChange(e.target.value)}
              rows={2}
              placeholder="Напишите комментарий…"
            />
            <Button
              type="button"
              variant={ButtonVariants.FILLED}
              color={ButtonColors.PRIMARY}
              size={ButtonSizes.SMALL}
              style={{ marginTop: '0.55rem' }}
              loading={commentPending}
              disabled={!commentText.trim()}
              onClick={onSubmitComment}
            >
              Отправить
            </Button>
          </div>
        </div>
        <div>
          <h3 className="subsection-head">Активность</h3>
          {activityLoading ? (
            <p className="muted">Загрузка…</p>
          ) : (
            <ul className="activity-list activity-list--scroll">
              {activity.map((a) => (
                <li key={a.id} className="activity-item">
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
