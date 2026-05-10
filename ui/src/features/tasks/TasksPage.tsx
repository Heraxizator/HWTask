import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { Loader2, LogOut, MessageSquare, Pencil, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import {
  createTask,
  deleteTask,
  getTask,
  listTasks,
  updateTask,
} from '../../api/tasks';
import { listActivity, listComments, postComment } from '../../api/collaboration';
import { ApiError, clearStoredToken } from '../../api/http';
import { listOrganizations, listProjects } from '../../api/workspace';
import type {
  CreateTaskRequest,
  TaskPriority,
  TaskResponse,
  TaskStatus,
  UpdateTaskRequest,
} from '../../types/task';

const PAGE_SIZE = 10;
const SORT = 'createdAt,desc';
const PROJECT_STORAGE_KEY = 'hwtask_selected_project_id';

const STATUS_OPTIONS: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
const PRIORITY_OPTIONS: (TaskPriority | '')[] = ['', 'LOW', 'MEDIUM', 'HIGH'];

function formatDt(iso: string): string {
  try {
    return new Intl.DateTimeFormat('ru-RU', {
      dateStyle: 'short',
      timeStyle: 'short',
      timeZone: 'UTC',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

function statusBadgeClass(s: TaskStatus): string {
  if (s === 'DONE') return 'badge badge-done';
  if (s === 'IN_PROGRESS') return 'badge badge-progress';
  return 'badge badge-todo';
}

function statusLabel(s: TaskStatus): string {
  if (s === 'TODO') return 'К выполнению';
  if (s === 'IN_PROGRESS') return 'В работе';
  return 'Готово';
}

function priorityLabel(p: TaskPriority | null): string {
  if (!p) return '—';
  if (p === 'LOW') return 'Низкий';
  if (p === 'MEDIUM') return 'Средний';
  return 'Высокий';
}

function toDatetimeLocalValue(iso: string | null): string {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

interface ProjectOption {
  label: string;
  projectId: string;
}

export function TasksPage({ onLogout }: { onLogout: () => void }) {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [projectId, setProjectId] = useState<string | null>(() =>
    localStorage.getItem(PROJECT_STORAGE_KEY),
  );
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [detailId, setDetailId] = useState<string | null>(null);
  const [commentText, setCommentText] = useState('');

  const workspaceQuery = useQuery({
    queryKey: ['workspace', 'flat-projects'],
    queryFn: async (): Promise<ProjectOption[]> => {
      const orgs = await listOrganizations();
      const pairs: ProjectOption[] = [];
      for (const o of orgs) {
        const ps = await listProjects(o.id);
        for (const p of ps) {
          pairs.push({ label: `${o.name} / ${p.name}`, projectId: p.id });
        }
      }
      return pairs;
    },
  });

  useEffect(() => {
    const opts = workspaceQuery.data;
    if (!opts?.length) return;
    const saved = localStorage.getItem(PROJECT_STORAGE_KEY);
    if (saved && opts.some((o) => o.projectId === saved)) {
      if (projectId !== saved) setProjectId(saved);
      return;
    }
    const first = opts[0].projectId;
    localStorage.setItem(PROJECT_STORAGE_KEY, first);
    setProjectId(first);
  }, [workspaceQuery.data, projectId]);

  const listQuery = useQuery({
    queryKey: ['tasks', 'list', projectId, page, PAGE_SIZE, SORT],
    queryFn: () =>
      listTasks({
        projectId: projectId!,
        page,
        size: PAGE_SIZE,
        sort: SORT,
      }),
    enabled: !!projectId,
  });

  const commentsQuery = useQuery({
    queryKey: ['tasks', 'comments', detailId],
    queryFn: () => listComments(detailId!),
    enabled: !!detailId,
  });

  const activityQuery = useQuery({
    queryKey: ['tasks', 'activity', detailId],
    queryFn: () => listActivity(detailId!),
    enabled: !!detailId,
  });

  const commentMut = useMutation({
    mutationFn: () => postComment(detailId!, commentText.trim()),
    onSuccess: () => {
      setCommentText('');
      void queryClient.invalidateQueries({ queryKey: ['tasks', 'comments', detailId] });
      void queryClient.invalidateQueries({ queryKey: ['tasks', 'activity', detailId] });
    },
  });

  const createMut = useMutation({
    mutationFn: (body: CreateTaskRequest) => createTask(body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['tasks'] });
      closeForm();
    },
    onError: onMutError,
  });

  const updateMut = useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateTaskRequest }) =>
      updateTask(id, body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['tasks'] });
      closeForm();
    },
    onError: onMutError,
  });

  const deleteMut = useMutation({
    mutationFn: (id: string) => deleteTask(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['tasks'] });
      setDeleteId(null);
      setDeleteError(null);
      setDetailId(null);
    },
    onError: (err: Error) => {
      const msg =
        err instanceof ApiError ? err.message : err.message || 'Ошибка удаления';
      setDeleteError(msg);
    },
  });

  function onMutError(err: Error) {
    const msg =
      err instanceof ApiError
        ? err.message
        : err.message || 'Не удалось выполнить операцию';
    setFormError(msg);
  }

  function closeForm() {
    setFormOpen(false);
    setEditingId(null);
    setFormError(null);
  }

  function openCreate() {
    setEditingId(null);
    setFormError(null);
    setFormOpen(true);
  }

  function openEdit(id: string) {
    setFormError(null);
    setEditingId(id);
    setFormOpen(true);
  }

  const totalPages = listQuery.data?.totalPages ?? 0;
  const tasks = listQuery.data?.content ?? [];
  const loadingList = listQuery.isLoading;
  const listErr = listQuery.error
    ? listQuery.error instanceof Error
      ? listQuery.error.message
      : 'Ошибка загрузки'
    : null;

  const projectOptions = workspaceQuery.data ?? [];

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <h1 className="app-title">HWTask</h1>
          <p className="app-sub">Проекты, роли и лента как в привычном портале</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
          <label className="muted" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            Проект
            <select
              value={projectId ?? ''}
              disabled={!projectOptions.length}
              onChange={(e) => {
                const v = e.target.value;
                setProjectId(v);
                localStorage.setItem(PROJECT_STORAGE_KEY, v);
                setPage(0);
              }}
            >
              {!projectOptions.length && <option value="">Нет проектов</option>}
              {projectOptions.map((o) => (
                <option key={o.projectId} value={o.projectId}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <button type="button" className="btn btn-primary" onClick={openCreate} disabled={!projectId}>
            <Plus size={18} strokeWidth={2} style={{ verticalAlign: 'middle', marginRight: '0.35rem' }} aria-hidden />
            Новая задача
          </button>
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => {
              clearStoredToken();
              onLogout();
            }}
          >
            <LogOut size={18} style={{ marginRight: '0.35rem' }} aria-hidden />
            Выход
          </button>
        </div>
      </header>

      {workspaceQuery.isLoading && (
        <div className="muted" style={{ marginBottom: '0.75rem' }}>
          Загрузка организаций…
        </div>
      )}
      {workspaceQuery.error && (
        <div className="alert" role="alert">
          Создайте организацию через API или войдите как демо-пользователь.
        </div>
      )}

      {listErr && (
        <div className="alert" role="alert">
          {listErr}
        </div>
      )}

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
                          onClick={() => setDetailId(detailId === t.id ? null : t.id)}
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
                      </td>
                      <td>
                        <span className={statusBadgeClass(t.status)}>{statusLabel(t.status)}</span>
                      </td>
                      <td className="muted">{t.dueAt ? formatDt(t.dueAt) : '—'}</td>
                      <td className="muted">{formatDt(t.updatedAt)}</td>
                      <td>
                        <div className="row-actions">
                          <button type="button" className="btn btn-ghost" onClick={() => void openEdit(t.id)} aria-label={`Редактировать: ${t.title}`}>
                            <Pencil size={16} aria-hidden />
                          </button>
                          <button type="button" className="btn btn-danger" onClick={() => setDeleteId(t.id)} aria-label={`Удалить: ${t.title}`}>
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
                Страница {(listQuery.data?.number ?? 0) + 1} из {Math.max(totalPages, 1)}
                {listQuery.data != null && ` · всего ${listQuery.data.totalElements}`}
              </span>
              <div className="pagination-controls">
                <button type="button" className="btn btn-ghost" disabled={page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
                  Назад
                </button>
                <button
                  type="button"
                  className="btn btn-ghost"
                  disabled={totalPages === 0 || page >= totalPages - 1}
                  onClick={() => setPage((p) => (totalPages > 0 && p < totalPages - 1 ? p + 1 : p))}
                >
                  Вперёд
                </button>
              </div>
            </footer>
          </>
        )}
      </section>

      {detailId && projectId && (
        <section className="panel" style={{ marginTop: '1rem' }}>
          <h2 style={{ marginTop: 0 }}>Карточка задачи</h2>
          <div style={{ display: 'grid', gap: '1.5rem', gridTemplateColumns: '1fr 1fr' }}>
            <div>
              <h3 className="muted" style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>
                Комментарии
              </h3>
              {commentsQuery.isLoading ? (
                <p className="muted">Загрузка…</p>
              ) : (
                <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                  {(commentsQuery.data ?? []).map((c) => (
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
                <textarea value={commentText} onChange={(e) => setCommentText(e.target.value)} rows={2} placeholder="Комментарий…" />
                <button type="button" className="btn btn-primary" style={{ marginTop: '0.5rem' }} disabled={!commentText.trim() || commentMut.isPending} onClick={() => commentMut.mutate()}>
                  Отправить
                </button>
              </div>
            </div>
            <div>
              <h3 className="muted" style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>
                Активность
              </h3>
              {activityQuery.isLoading ? (
                <p className="muted">Загрузка…</p>
              ) : (
                <ul style={{ listStyle: 'none', padding: 0, margin: 0, maxHeight: 280, overflow: 'auto' }}>
                  {(activityQuery.data ?? []).map((a) => (
                    <li key={a.id} style={{ marginBottom: '0.5rem', fontSize: '0.9rem' }}>
                      <span className="muted">{formatDt(a.createdAt)}</span> — {a.summary}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </section>
      )}

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>

      {formOpen && projectId && (
        <TaskFormModal
          key={editingId ?? 'create'}
          projectId={projectId}
          editingId={editingId}
          onClose={closeForm}
          onError={setFormError}
          errorMessage={formError}
          createMut={createMut}
          updateMut={updateMut}
        />
      )}

      {deleteId && (
        <ConfirmDeleteModal
          title="Удалить задачу?"
          description="Действие нельзя отменить."
          errorMessage={deleteError}
          loading={deleteMut.isPending}
          onCancel={() => {
            setDeleteId(null);
            setDeleteError(null);
          }}
          onConfirm={() => deleteMut.mutate(deleteId)}
        />
      )}
    </div>
  );
}

function TaskFormModal({
  projectId,
  editingId,
  onClose,
  onError,
  errorMessage,
  createMut,
  updateMut,
}: {
  projectId: string;
  editingId: string | null;
  onClose: () => void;
  onError: (msg: string | null) => void;
  errorMessage: string | null;
  createMut: ReturnType<typeof useMutation<TaskResponse, Error, CreateTaskRequest>>;
  updateMut: ReturnType<typeof useMutation<TaskResponse, Error, { id: string; body: UpdateTaskRequest }>>;
}) {
  const isEdit = editingId != null;
  const detailQuery = useQuery({
    queryKey: ['tasks', 'detail', editingId],
    queryFn: () => getTask(editingId!),
    enabled: isEdit,
  });

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [priority, setPriority] = useState<TaskPriority | ''>('');
  const [dueLocal, setDueLocal] = useState('');

  useEffect(() => {
    if (detailQuery.data) {
      const d = detailQuery.data;
      setTitle(d.title);
      setDescription(d.description ?? '');
      setStatus(d.status);
      setPriority((d.priority ?? '') as TaskPriority | '');
      setDueLocal(toDatetimeLocalValue(d.dueAt));
    }
  }, [detailQuery.data]);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    onError(null);
    const t = title.trim();
    if (!t) {
      onError('Укажите название задачи');
      return;
    }

    const dueIso = dueLocal ? new Date(dueLocal).toISOString() : null;

    if (isEdit && editingId) {
      const d = detailQuery.data;
      const co =
        d?.extraMembers.filter((m) => m.role === 'CO_ASSIGNEE').map((m) => m.userId) ?? [];
      const obs =
        d?.extraMembers.filter((m) => m.role === 'OBSERVER').map((m) => m.userId) ?? [];
      const body: UpdateTaskRequest = {
        title: t,
        description: description.trim() || null,
        status,
        priority: priority === '' ? null : priority,
        assigneeId: d?.assigneeId ?? null,
        dueAt: dueIso,
        coAssigneeIds: co,
        observerIds: obs,
      };
      updateMut.mutate({ id: editingId, body });
    } else {
      const body: CreateTaskRequest = {
        projectId,
        title: t,
        description: description.trim() || null,
        status,
        priority: priority === '' ? null : priority,
        dueAt: dueIso,
        coAssigneeIds: [],
        observerIds: [],
      };
      createMut.mutate(body);
    }
  }

  const pending = createMut.isPending || updateMut.isPending;
  const loadDetail = isEdit && detailQuery.isLoading;

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(ev) => {
        if (ev.target === ev.currentTarget) onClose();
      }}
    >
      <div className="modal panel" role="dialog" aria-modal="true" aria-labelledby="task-form-title" onClick={(e) => e.stopPropagation()}>
        <h2 id="task-form-title">{isEdit ? 'Редактировать' : 'Новая задача'}</h2>

        {loadDetail ? (
          <div className="state-block">
            <Loader2 size={24} style={{ animation: 'spin 0.9s linear infinite', color: 'var(--color-accent)' }} aria-hidden />
          </div>
        ) : (
          <form onSubmit={(e) => void handleSubmit(e)}>
            {errorMessage && (
              <div className="alert" role="alert" style={{ marginBottom: '1rem' }}>
                {errorMessage}
              </div>
            )}

            <div className="field">
              <label htmlFor="task-title">Название</label>
              <input id="task-title" name="title" value={title} onChange={(e) => setTitle(e.target.value)} autoComplete="off" maxLength={255} required />
            </div>

            <div className="field">
              <label htmlFor="task-desc">Описание</label>
              <textarea id="task-desc" name="description" value={description} onChange={(e) => setDescription(e.target.value)} maxLength={10000} />
            </div>

            <div className="field">
              <label htmlFor="task-due">Срок</label>
              <input id="task-due" type="datetime-local" value={dueLocal} onChange={(e) => setDueLocal(e.target.value)} />
            </div>

            <div className="field">
              <label htmlFor="task-status">Статус</label>
              <select id="task-status" name="status" value={status} onChange={(e) => setStatus(e.target.value as TaskStatus)}>
                {STATUS_OPTIONS.map((s) => (
                  <option key={s} value={s}>
                    {statusLabel(s)}
                  </option>
                ))}
              </select>
            </div>

            <div className="field">
              <label htmlFor="task-priority">Приоритет</label>
              <select id="task-priority" name="priority" value={priority} onChange={(e) => setPriority((e.target.value || '') as TaskPriority | '')}>
                <option value="">Не задан</option>
                {PRIORITY_OPTIONS.filter(Boolean).map((p) => (
                  <option key={p} value={p}>
                    {priorityLabel(p as TaskPriority)}
                  </option>
                ))}
              </select>
            </div>

            <div className="modal-actions">
              <button type="button" className="btn btn-ghost" onClick={onClose} disabled={pending}>
                Отмена
              </button>
              <button type="submit" className="btn btn-primary" disabled={pending}>
                {pending ? 'Сохранение…' : isEdit ? 'Сохранить' : 'Создать'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

function ConfirmDeleteModal({
  title,
  description,
  errorMessage,
  loading,
  onCancel,
  onConfirm,
}: {
  title: string;
  description: string;
  errorMessage: string | null;
  loading: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}) {
  return (
    <div className="modal-backdrop" role="presentation" onClick={(ev) => {
      if (ev.target === ev.currentTarget) onCancel();
    }}>
      <div className="modal panel" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
        <h2 id="confirm-title">{title}</h2>
        <p style={{ color: 'var(--color-ice-muted)', marginTop: 0 }}>{description}</p>
        {errorMessage && (
          <div className="alert" role="alert" style={{ marginTop: '1rem' }}>
            {errorMessage}
          </div>
        )}
        <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
          <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={loading}>
            Отмена
          </button>
          <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={loading}>
            {loading ? 'Удаление…' : 'Удалить'}
          </button>
        </div>
      </div>
    </div>
  );
}
