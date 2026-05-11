import { fetchJson, fetchVoid } from './http';

export interface ChecklistItemResponse {
  id: string;
  title: string;
  done: boolean;
  sortOrder: number;
}

const base = (taskId: string) =>
  `/api/v1/tasks/${encodeURIComponent(taskId)}/checklist-items`;

export function listChecklist(taskId: string): Promise<ChecklistItemResponse[]> {
  return fetchJson<ChecklistItemResponse[]>(base(taskId));
}

export function createChecklistItem(
  taskId: string,
  title: string,
): Promise<ChecklistItemResponse> {
  return fetchJson<ChecklistItemResponse>(base(taskId), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title }),
  });
}

export function updateChecklistItem(
  taskId: string,
  itemId: string,
  body: { title?: string; done?: boolean; sortOrder?: number },
): Promise<ChecklistItemResponse> {
  return fetchJson<ChecklistItemResponse>(
    `${base(taskId)}/${encodeURIComponent(itemId)}`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    },
  );
}

export function deleteChecklistItem(taskId: string, itemId: string): Promise<void> {
  return fetchVoid(`${base(taskId)}/${encodeURIComponent(itemId)}`, {
    method: 'DELETE',
  });
}
