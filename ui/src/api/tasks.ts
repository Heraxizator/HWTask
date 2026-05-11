import type {
  CreateTaskRequest,
  PageTaskResponse,
  TaskResponse,
  UpdateTaskRequest,
} from '../types/task';
import { fetchJson, fetchVoid } from './http';

const BASE = '/api/v1/tasks';

export interface ListTasksParams {
  projectId: string;
  page?: number;
  size?: number;
  sort?: string;
  /** Поиск по названию и описанию */
  q?: string;
  /** Фильтр по любому из тегов */
  tagIds?: string[];
}

export function listTasks(params: ListTasksParams): Promise<PageTaskResponse> {
  const sp = new URLSearchParams();
  sp.set('projectId', params.projectId);
  if (params.page !== undefined) sp.set('page', String(params.page));
  if (params.size !== undefined) sp.set('size', String(params.size));
  if (params.sort) sp.set('sort', params.sort);
  if (params.q?.trim()) sp.set('q', params.q.trim());
  if (params.tagIds?.length) {
    for (const id of params.tagIds) {
      sp.append('tagIds', id);
    }
  }
  return fetchJson<PageTaskResponse>(`${BASE}?${sp.toString()}`);
}

export function getTask(id: string): Promise<TaskResponse> {
  return fetchJson<TaskResponse>(`${BASE}/${encodeURIComponent(id)}`);
}

export function createTask(body: CreateTaskRequest): Promise<TaskResponse> {
  return fetchJson<TaskResponse>(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}

export function updateTask(
  id: string,
  body: UpdateTaskRequest,
): Promise<TaskResponse> {
  return fetchJson<TaskResponse>(`${BASE}/${encodeURIComponent(id)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
}

export function deleteTask(id: string): Promise<void> {
  return fetchVoid(`${BASE}/${encodeURIComponent(id)}`, { method: 'DELETE' });
}
