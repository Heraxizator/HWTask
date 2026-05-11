import { fetchJson, fetchVoid } from './http';

export interface TaskTrashEntryResponse {
  id: string;
  projectId: string;
  title: string;
  deletedAt: string | null;
}

export interface PageTrashResponse {
  content: TaskTrashEntryResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export function listDeletedTasks(
  projectId: string,
  page = 0,
  size = 20,
): Promise<PageTrashResponse> {
  const sp = new URLSearchParams();
  sp.set('page', String(page));
  sp.set('size', String(size));
  return fetchJson<PageTrashResponse>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/deleted-tasks?${sp.toString()}`,
  );
}

export function restoreTask(taskId: string): Promise<void> {
  return fetchVoid(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/restore`,
    { method: 'POST' },
  );
}

export function purgeTaskPermanent(taskId: string): Promise<void> {
  return fetchVoid(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/permanent`,
    { method: 'DELETE' },
  );
}
