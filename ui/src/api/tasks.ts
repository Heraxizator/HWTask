import type {
  CreateTaskRequest,
  PageTaskResponse,
  TaskResponse,
  UpdateTaskRequest,
} from '../types/task';
import { fetchJson, parseJsonResponse } from './http';

const BASE = '/api/v1/tasks';

export interface ListTasksParams {
  page?: number;
  size?: number;
  sort?: string;
}

export function listTasks(params: ListTasksParams): Promise<PageTaskResponse> {
  const sp = new URLSearchParams();
  if (params.page !== undefined) sp.set('page', String(params.page));
  if (params.size !== undefined) sp.set('size', String(params.size));
  if (params.sort) sp.set('sort', params.sort);
  const q = sp.toString();
  const url = q ? `${BASE}?${q}` : BASE;
  return fetchJson<PageTaskResponse>(url);
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

export async function deleteTask(id: string): Promise<void> {
  const res = await fetch(`${BASE}/${encodeURIComponent(id)}`, {
    method: 'DELETE',
    headers: { Accept: 'application/json' },
  });
  if (res.ok && res.status === 204) return;
  await parseJsonResponse<unknown>(res);
}
