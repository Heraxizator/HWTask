import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';

import { ApiError } from '../../../api/http';
import { createProjectTag, listProjectTags, setTaskTags } from '../../../api/tags';
import { getTask } from '../../../api/tasks';
import type {
  CreateTaskRequest,
  TaskPriority,
  TaskResponse,
  TaskStatus,
  UpdateTaskRequest,
} from '../../../types/task';
import { PRIORITY_OPTIONS, STATUS_OPTIONS, priorityLabel, statusLabel, toDatetimeLocalValue } from '../taskDisplay';

export function TaskFormModal({
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
  const queryClient = useQueryClient();
  const isEdit = editingId != null;
  const detailQuery = useQuery({
    queryKey: ['tasks', 'detail', editingId],
    queryFn: () => getTask(editingId!),
    enabled: isEdit,
  });

  const projectTagsQuery = useQuery({
    queryKey: ['tags', projectId],
    queryFn: () => listProjectTags(projectId),
  });

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [priority, setPriority] = useState<TaskPriority | ''>('');
  const [dueLocal, setDueLocal] = useState('');
  const [selectedTagIds, setSelectedTagIds] = useState<string[]>([]);
  const [newTagName, setNewTagName] = useState('');

  const createTagMut = useMutation({
    mutationFn: (name: string) => createProjectTag(projectId, name.trim()),
    onSuccess: (tag) => {
      void queryClient.invalidateQueries({ queryKey: ['tags', projectId] });
      setSelectedTagIds((prev) => [...prev, tag.id]);
      setNewTagName('');
    },
    onError: (err: Error) => {
      const msg =
        err instanceof ApiError ? err.message : err.message || 'Не удалось создать тег';
      onError(msg);
    },
  });

  useEffect(() => {
    if (detailQuery.data) {
      const d = detailQuery.data;
      setTitle(d.title);
      setDescription(d.description ?? '');
      setStatus(d.status);
      setPriority((d.priority ?? '') as TaskPriority | '');
      setDueLocal(toDatetimeLocalValue(d.dueAt));
      setSelectedTagIds(d.tags.map((x) => x.id));
    }
  }, [detailQuery.data]);

  async function handleSubmit(e: FormEvent) {
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
      try {
        await updateMut.mutateAsync({ id: editingId, body });
        await setTaskTags(editingId, selectedTagIds);
        void queryClient.invalidateQueries({ queryKey: ['tasks'] });
        void queryClient.invalidateQueries({ queryKey: ['tasks', 'detail', editingId] });
        onClose();
      } catch {
        /* updateMut.onError */
      }
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
        tagIds: selectedTagIds.length ? selectedTagIds : undefined,
      };
      createMut.mutate(body);
    }
  }

  const pending =
    createMut.isPending ||
    updateMut.isPending ||
    createTagMut.isPending;
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

            <div className="field">
              <span className="muted" style={{ fontSize: '0.9rem', display: 'block', marginBottom: '0.35rem' }}>
                Теги
              </span>
              {projectTagsQuery.isLoading ? (
                <p className="muted" style={{ margin: 0 }}>Загрузка тегов…</p>
              ) : (projectTagsQuery.data ?? []).length === 0 ? (
                <p className="muted" style={{ margin: 0 }}>Пока нет тегов — создайте ниже.</p>
              ) : (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                  {(projectTagsQuery.data ?? []).map((tag) => (
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
                        checked={selectedTagIds.includes(tag.id)}
                        onChange={(e) => {
                          const on = e.target.checked;
                          setSelectedTagIds((prev) =>
                            on ? [...prev, tag.id] : prev.filter((id) => id !== tag.id),
                          );
                        }}
                      />
                      {tag.name}
                    </label>
                  ))}
                </div>
              )}
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.65rem' }}>
                <input
                  type="text"
                  value={newTagName}
                  onChange={(e) => setNewTagName(e.target.value)}
                  placeholder="Новый тег…"
                  maxLength={64}
                  style={{ flex: '1 1 160px', minWidth: 0 }}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      const n = newTagName.trim();
                      if (n) createTagMut.mutate(n);
                    }
                  }}
                />
                <button
                  type="button"
                  className="btn btn-ghost"
                  disabled={!newTagName.trim() || createTagMut.isPending}
                  onClick={() => createTagMut.mutate(newTagName.trim())}
                >
                  Создать тег
                </button>
              </div>
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
