import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { Loader2, Pencil, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import {
  createTask,
  deleteTask,
  getTask,
  listTasks,
  updateTask,
} from '../../api/tasks';
import { ApiError } from '../../api/http';
import type {
  CreateTaskRequest,
  TaskPriority,
  TaskResponse,
  TaskStatus,
  UpdateTaskRequest,
} from '../../types/task';

const PAGE_SIZE = 10;
const SORT = 'createdAt,desc';

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

export function TasksPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  const listQuery = useQuery({
    queryKey: ['tasks', 'list', page, PAGE_SIZE, SORT],
    queryFn: () => listTasks({ page, size: PAGE_SIZE, sort: SORT }),
  });

  const totalPages = listQuery.data?.totalPages ?? 0;
  const tasks = listQuery.data?.content ?? [];

  const createMut = useMutation({
    mutationFn: (body: CreateTaskRequest) => createTask(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      closeForm();
    },
    onError: onMutError,
  });

  const updateMut = useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateTaskRequest }) =>
      updateTask(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      closeForm();
    },
    onError: onMutError,
  });

  const deleteMut = useMutation({
    mutationFn: (id: string) => deleteTask(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      setDeleteId(null);
      setDeleteError(null);
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

  const loadingList = listQuery.isLoading;
  const listErr = listQuery.error
    ? listQuery.error instanceof Error
      ? listQuery.error.message
      : 'Ошибка загрузки'
    : null;

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <h1 className="app-title">HWTask</h1>
          <p className="app-sub">Металл и лёд · задачи в одном списке</p>
        </div>
        <button type="button" className="btn btn-primary" onClick={openCreate}>
          <Plus
            size={18}
            strokeWidth={2}
            style={{ verticalAlign: 'middle', marginRight: '0.35rem' }}
            aria-hidden
          />
          Новая задача
        </button>
      </header>

      {listErr && (
        <div className="alert" role="alert">
          {listErr}
        </div>
      )}

      <section className="panel">
        {loadingList ? (
          <div className="state-block" aria-live="polite">
            <Loader2
              size={28}
              className="spin"
              style={{
                animation: 'spin 0.9s linear infinite',
                color: 'var(--color-accent)',
                marginBottom: '0.75rem',
              }}
              aria-hidden
            />
            <span className="sr-only">Загрузка списка</span>
            <strong>Загрузка…</strong>
          </div>
        ) : tasks.length === 0 && !listErr ? (
          <div className="state-block">
            <p style={{ margin: 0 }}>
              Пока нет задач. Создайте первую — кнопка{' '}
              <strong>Новая задача</strong>.
            </p>
          </div>
        ) : (
          <>
            <div className="table-wrap">
              <table className="tasks-table">
                <thead>
                  <tr>
                    <th>Задача</th>
                    <th>Статус</th>
                    <th>Приоритет</th>
                    <th>Обновлено (UTC)</th>
                    <th style={{ width: '1%', textAlign: 'right' }} aria-label="Действия" />
                  </tr>
                </thead>
                <tbody>
                  {tasks.map((t: TaskResponse) => (
                    <tr key={t.id}>
                      <td>
                        <div style={{ fontWeight: 600 }}>{t.title}</div>
                        {t.description && (
                          <div className="muted" style={{ marginTop: '0.2rem' }}>
                            {t.description.length > 120
                              ? `${t.description.slice(0, 117)}…`
                              : t.description}
                          </div>
                        )}
                      </td>
                      <td>
                        <span className={statusBadgeClass(t.status)}>
                          {statusLabel(t.status)}
                        </span>
                      </td>
                      <td className="muted">{priorityLabel(t.priority)}</td>
                      <td className="muted">{formatDt(t.updatedAt)}</td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="btn btn-ghost"
                            onClick={() => void openEdit(t.id)}
                            aria-label={`Редактировать: ${t.title}`}
                          >
                            <Pencil size={16} aria-hidden />
                          </button>
                          <button
                            type="button"
                            className="btn btn-danger"
                            onClick={() => setDeleteId(t.id)}
                            aria-label={`Удалить: ${t.title}`}
                          >
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
                Страница {(listQuery.data?.number ?? 0) + 1} из{' '}
                {Math.max(totalPages, 1)}
                {listQuery.data != null &&
                  ` · всего ${listQuery.data.totalElements}`}
              </span>
              <div className="pagination-controls">
                <button
                  type="button"
                  className="btn btn-ghost"
                  disabled={page <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                >
                  Назад
                </button>
                <button
                  type="button"
                  className="btn btn-ghost"
                  disabled={totalPages === 0 || page >= totalPages - 1}
                  onClick={() =>
                    setPage((p) =>
                      totalPages > 0 && p < totalPages - 1 ? p + 1 : p,
                    )
                  }
                >
                  Вперёд
                </button>
              </div>
            </footer>
          </>
        )}
      </section>

      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>

      {formOpen && (
        <TaskFormModal
          key={editingId ?? 'create'}
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
  editingId,
  onClose,
  onError,
  errorMessage,
  createMut,
  updateMut,
}: {
  editingId: string | null;
  onClose: () => void;
  onError: (msg: string | null) => void;
  errorMessage: string | null;
  createMut: ReturnType<typeof useMutation<TaskResponse, Error, CreateTaskRequest>>;
  updateMut: ReturnType<
    typeof useMutation<
      TaskResponse,
      Error,
      { id: string; body: UpdateTaskRequest }
    >
  >;
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

  useEffect(() => {
    if (detailQuery.data) {
      const d = detailQuery.data;
      setTitle(d.title);
      setDescription(d.description ?? '');
      setStatus(d.status);
      setPriority((d.priority ?? '') as TaskPriority | '');
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

    if (isEdit && editingId) {
      const body: UpdateTaskRequest = {
        title: t,
        description: description.trim() || null,
        status,
        priority: priority === '' ? null : priority,
      };
      updateMut.mutate({ id: editingId, body });
    } else {
      const body: CreateTaskRequest = {
        title: t,
        description: description.trim() || null,
        status,
        priority: priority === '' ? null : priority,
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
      <div
        className="modal panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="task-form-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="task-form-title">{isEdit ? 'Редактировать' : 'Новая задача'}</h2>

        {loadDetail ? (
          <div className="state-block">
            <Loader2
              size={24}
              style={{
                animation: 'spin 0.9s linear infinite',
                color: 'var(--color-accent)',
              }}
              aria-hidden
            />
            <p className="sr-only">Загрузка задачи</p>
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
              <input
                id="task-title"
                name="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                autoComplete="off"
                maxLength={255}
                required
              />
            </div>

            <div className="field">
              <label htmlFor="task-desc">Описание</label>
              <textarea
                id="task-desc"
                name="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                maxLength={10000}
              />
            </div>

            <div className="field">
              <label htmlFor="task-status">Статус</label>
              <select
                id="task-status"
                name="status"
                value={status}
                onChange={(e) => setStatus(e.target.value as TaskStatus)}
              >
                {STATUS_OPTIONS.map((s) => (
                  <option key={s} value={s}>
                    {statusLabel(s)}
                  </option>
                ))}
              </select>
            </div>

            <div className="field">
              <label htmlFor="task-priority">Приоритет</label>
              <select
                id="task-priority"
                name="priority"
                value={priority}
                onChange={(e) =>
                  setPriority((e.target.value || '') as TaskPriority | '')
                }
              >
                <option value="">Не задан</option>
                {PRIORITY_OPTIONS.filter(Boolean).map((p) => (
                  <option key={p} value={p}>
                    {priorityLabel(p as TaskPriority)}
                  </option>
                ))}
              </select>
            </div>

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={onClose}
                disabled={pending}
              >
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
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(ev) => {
        if (ev.target === ev.currentTarget) onCancel();
      }}
    >
      <div
        className="modal panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-title"
      >
        <h2 id="confirm-title">{title}</h2>
        <p style={{ color: 'var(--color-ice-muted)', marginTop: 0 }}>{description}</p>
        {errorMessage && (
          <div className="alert" role="alert" style={{ marginTop: '1rem' }}>
            {errorMessage}
          </div>
        )}
        <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
          <button
            type="button"
            className="btn btn-ghost"
            onClick={onCancel}
            disabled={loading}
          >
            Отмена
          </button>
          <button
            type="button"
            className="btn btn-danger"
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? 'Удаление…' : 'Удалить'}
          </button>
        </div>
      </div>
    </div>
  );
}
