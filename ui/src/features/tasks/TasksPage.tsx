import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { useEffect, useState } from 'react';

import {
  createChecklistItem,
  deleteChecklistItem,
  listChecklist,
  updateChecklistItem,
} from '../../api/checklist';
import { listActivity, listComments, postComment } from '../../api/collaboration';
import {
  getUnreadNotificationCount,
  listNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  muteTaskNotifications,
  unmuteTaskNotifications,
} from '../../api/notifications';
import { listProjectTags, setTaskTags } from '../../api/tags';
import {
  listDeletedTasks,
  purgeTaskPermanent,
  restoreTask,
} from '../../api/trash';
import {
  listTaskTimeEntries,
  startTimeTracking,
  stopTimeTracking,
} from '../../api/time';
import {
  createTask,
  deleteTask,
  getTask,
  listTasks,
  updateTask,
} from '../../api/tasks';
import { ApiError, clearStoredToken } from '../../api/http';
import { listOrganizations, listProjects } from '../../api/workspace';
import type { CreateTaskRequest, UpdateTaskRequest } from '../../types/task';
import { ConfirmDeleteModal } from './components/ConfirmDeleteModal';
import { TaskDetailPanel } from './components/TaskDetailPanel';
import { TaskFormModal } from './components/TaskFormModal';
import { TasksHeader, type ProjectOption } from './components/TasksHeader';
import { TasksTableSection } from './components/TasksTableSection';
import { TrashModal } from './components/TrashModal';

const PAGE_SIZE = 10;
const SORT = 'createdAt,desc';
const PROJECT_STORAGE_KEY = 'hwtask_selected_project_id';

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
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [filterTagIds, setFilterTagIds] = useState<string[]>([]);
  const [notifOpen, setNotifOpen] = useState(false);
  const [trashOpen, setTrashOpen] = useState(false);
  const [checklistNewTitle, setChecklistNewTitle] = useState('');

  useEffect(() => {
    const t = window.setTimeout(() => setDebouncedSearch(searchInput.trim()), 400);
    return () => window.clearTimeout(t);
  }, [searchInput]);

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
    queryKey: [
      'tasks',
      'list',
      projectId,
      page,
      PAGE_SIZE,
      SORT,
      debouncedSearch,
      filterTagIds.join(','),
    ],
    queryFn: () =>
      listTasks({
        projectId: projectId!,
        page,
        size: PAGE_SIZE,
        sort: SORT,
        q: debouncedSearch || undefined,
        tagIds: filterTagIds.length ? filterTagIds : undefined,
      }),
    enabled: !!projectId,
  });

  const tagsQuery = useQuery({
    queryKey: ['tags', projectId],
    queryFn: () => listProjectTags(projectId!),
    enabled: !!projectId,
  });

  const unreadQuery = useQuery({
    queryKey: ['notifications', 'unread'],
    queryFn: getUnreadNotificationCount,
    refetchInterval: 45_000,
  });

  const notificationsQuery = useQuery({
    queryKey: ['notifications', 'list'],
    queryFn: () => listNotifications(0, 15),
    enabled: notifOpen,
  });

  const trashQuery = useQuery({
    queryKey: ['trash', projectId],
    queryFn: () => listDeletedTasks(projectId!, 0, 20),
    enabled: !!projectId && trashOpen,
  });

  const checklistQuery = useQuery({
    queryKey: ['checklist', detailId],
    queryFn: () => listChecklist(detailId!),
    enabled: !!detailId,
  });

  const timeEntriesQuery = useQuery({
    queryKey: ['time-entries', detailId],
    queryFn: () => listTaskTimeEntries(detailId!),
    enabled: !!detailId,
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

  const detailTaskQuery = useQuery({
    queryKey: ['tasks', 'detail', detailId],
    queryFn: () => getTask(detailId!),
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
    },
    onError: onMutError,
  });

  const markAllReadMut = useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notifications'] });
      void queryClient.invalidateQueries({ queryKey: ['notifications', 'unread'] });
    },
  });

  const markOneReadMut = useMutation({
    mutationFn: markNotificationRead,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['notifications'] });
      void queryClient.invalidateQueries({ queryKey: ['notifications', 'unread'] });
    },
  });

  const restoreMut = useMutation({
    mutationFn: restoreTask,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['tasks'] });
      void queryClient.invalidateQueries({ queryKey: ['trash'] });
    },
  });

  const purgeMut = useMutation({
    mutationFn: purgeTaskPermanent,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['trash'] });
    },
  });

  const startTimeMut = useMutation({
    mutationFn: () => startTimeTracking(detailId!),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['time-entries', detailId] });
    },
  });

  const stopTimeMut = useMutation({
    mutationFn: stopTimeTracking,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['time-entries'] });
      void queryClient.invalidateQueries({ queryKey: ['time-entries', detailId] });
    },
  });

  const muteMut = useMutation({
    mutationFn: () => muteTaskNotifications(detailId!),
  });

  const unmuteMut = useMutation({
    mutationFn: () => unmuteTaskNotifications(detailId!),
  });

  const addChecklistMut = useMutation({
    mutationFn: (title: string) => createChecklistItem(detailId!, title),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['checklist', detailId] });
    },
  });

  const toggleCheckMut = useMutation({
    mutationFn: ({ id, done }: { id: string; done: boolean }) =>
      updateChecklistItem(detailId!, id, { done }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['checklist', detailId] });
    },
  });

  const deleteCheckMut = useMutation({
    mutationFn: (itemId: string) => deleteChecklistItem(detailId!, itemId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['checklist', detailId] });
    },
  });

  const setTagsMut = useMutation({
    mutationFn: ({ taskId, tagIds }: { taskId: string; tagIds: string[] }) =>
      setTaskTags(taskId, tagIds),
    onSuccess: (_data, vars) => {
      void queryClient.invalidateQueries({ queryKey: ['tasks'] });
      void queryClient.invalidateQueries({ queryKey: ['tasks', 'detail', vars.taskId] });
    },
  });

  const deleteMut = useMutation({
    mutationFn: (id: string) => deleteTask(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['tasks'] });
      void queryClient.invalidateQueries({ queryKey: ['trash'] });
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
  const unreadCount = unreadQuery.data?.count ?? 0;
  const notificationItems = notificationsQuery.data?.content ?? [];
  const trashItems = trashQuery.data?.content ?? [];
  const detailTags = detailTaskQuery.data?.tags ?? [];
  const timeEntries = timeEntriesQuery.data ?? [];
  const activeTimerEntry = timeEntries.find((e) => e.endedAt == null) ?? null;

  function toggleFilterTag(id: string) {
    setFilterTagIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    );
    setPage(0);
  }

  function toggleDetailTag(tagId: string, checked: boolean) {
    if (!detailId) return;
    const ids = new Set(detailTags.map((t) => t.id));
    if (checked) ids.add(tagId);
    else ids.delete(tagId);
    setTagsMut.mutate({ taskId: detailId, tagIds: [...ids] });
  }

  function addChecklistFromField() {
    const v = checklistNewTitle.trim();
    if (!v) return;
    addChecklistMut.mutate(v);
    setChecklistNewTitle('');
  }

  return (
    <div className="app-shell">
      <TasksHeader
        projectOptions={projectOptions}
        projectId={projectId}
        onProjectChange={(v) => {
          setProjectId(v);
          localStorage.setItem(PROJECT_STORAGE_KEY, v);
          setPage(0);
        }}
        onOpenCreate={openCreate}
        notifOpen={notifOpen}
        onToggleNotifs={() => setNotifOpen((x) => !x)}
        unreadCount={unreadCount}
        notificationsLoading={notificationsQuery.isLoading}
        notificationItems={notificationItems}
        markAllPending={markAllReadMut.isPending}
        onMarkAllRead={() => markAllReadMut.mutate()}
        onNotificationActivate={(n) => {
          if (!n.read) markOneReadMut.mutate(n.id);
          if (n.taskId) setDetailId(n.taskId);
        }}
        onOpenTrash={() => setTrashOpen(true)}
        onLogout={() => {
          clearStoredToken();
          onLogout();
        }}
      />

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

      <TasksTableSection
        projectId={projectId}
        loadingList={loadingList}
        listErr={listErr}
        tasks={tasks}
        searchInput={searchInput}
        onSearchChange={(v) => {
          setSearchInput(v);
          setPage(0);
        }}
        projectTags={tagsQuery.data ?? []}
        filterTagIds={filterTagIds}
        onToggleFilterTag={toggleFilterTag}
        detailId={detailId}
        onToggleDetail={(taskId) => setDetailId(detailId === taskId ? null : taskId)}
        onEdit={openEdit}
        onRequestDelete={setDeleteId}
        page={page}
        totalPages={totalPages}
        listNumber={listQuery.data?.number ?? 0}
        totalElements={listQuery.data?.totalElements}
        onPagePrev={() => setPage((p) => Math.max(0, p - 1))}
        onPageNext={() =>
          setPage((p) => (totalPages > 0 && p < totalPages - 1 ? p + 1 : p))
        }
      />

      {detailId && projectId && (
        <TaskDetailPanel
          tagsLoading={tagsQuery.isLoading}
          detailLoading={detailTaskQuery.isLoading}
          projectTags={tagsQuery.data ?? []}
          detailTags={detailTags}
          tagsMutationPending={setTagsMut.isPending}
          onToggleDetailTag={toggleDetailTag}
          timeLoading={timeEntriesQuery.isLoading}
          timeEntries={timeEntries}
          activeTimerEntry={activeTimerEntry}
          startTimePending={startTimeMut.isPending}
          stopTimePending={stopTimeMut.isPending}
          onStartTime={() => startTimeMut.mutate()}
          onStopTime={() => stopTimeMut.mutate()}
          checklistLoading={checklistQuery.isLoading}
          checklistItems={checklistQuery.data ?? []}
          checklistNewTitle={checklistNewTitle}
          onChecklistTitleChange={setChecklistNewTitle}
          onAddChecklist={addChecklistFromField}
          checklistAddPending={addChecklistMut.isPending}
          toggleCheckPending={toggleCheckMut.isPending}
          deleteCheckPending={deleteCheckMut.isPending}
          onToggleChecklistItem={(id, done) => toggleCheckMut.mutate({ id, done })}
          onDeleteChecklistItem={(id) => deleteCheckMut.mutate(id)}
          commentsLoading={commentsQuery.isLoading}
          comments={commentsQuery.data ?? []}
          commentText={commentText}
          onCommentChange={setCommentText}
          commentPending={commentMut.isPending}
          onSubmitComment={() => commentMut.mutate()}
          activityLoading={activityQuery.isLoading}
          activity={activityQuery.data ?? []}
          mutePending={muteMut.isPending}
          unmutePending={unmuteMut.isPending}
          onMute={() => muteMut.mutate()}
          onUnmute={() => unmuteMut.mutate()}
        />
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
          description="Задача попадёт в корзину; её можно восстановить или удалить навсегда оттуда."
          errorMessage={deleteError}
          loading={deleteMut.isPending}
          onCancel={() => {
            setDeleteId(null);
            setDeleteError(null);
          }}
          onConfirm={() => deleteMut.mutate(deleteId)}
        />
      )}

      {trashOpen && projectId && (
        <TrashModal
          loading={trashQuery.isLoading}
          items={trashItems}
          restorePending={restoreMut.isPending}
          purgePending={purgeMut.isPending}
          onClose={() => setTrashOpen(false)}
          onRestore={(id) => restoreMut.mutate(id)}
          onPurge={(id) => purgeMut.mutate(id)}
        />
      )}
    </div>
  );
}
