import { fetchJson, fetchVoid } from './http';

export type NotificationType =
  | 'TASK_CREATED'
  | 'TASK_UPDATED'
  | 'TASK_STATUS_CHANGED'
  | 'TASK_DELETED'
  | 'TASK_COMMENT'
  | 'REMINDER';

export interface NotificationResponse {
  id: string;
  taskId: string | null;
  type: NotificationType;
  title: string;
  body: string | null;
  read: boolean;
  createdAt: string;
}

export interface UnreadCountResponse {
  count: number;
}

const BASE = '/api/v1/me/notifications';

export function listNotifications(page = 0, size = 20): Promise<{
  content: NotificationResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
}> {
  const sp = new URLSearchParams();
  sp.set('page', String(page));
  sp.set('size', String(size));
  return fetchJson(`${BASE}?${sp.toString()}`);
}

export function getUnreadNotificationCount(): Promise<UnreadCountResponse> {
  return fetchJson<UnreadCountResponse>(`${BASE}/unread-count`);
}

export function markAllNotificationsRead(): Promise<void> {
  return fetchVoid(`${BASE}/read-all`, { method: 'PATCH' });
}

export function markNotificationRead(id: string): Promise<void> {
  return fetchVoid(`${BASE}/${encodeURIComponent(id)}/read`, {
    method: 'PATCH',
  });
}

export function muteTaskNotifications(taskId: string): Promise<void> {
  return fetchVoid(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/notifications/mute`,
    { method: 'POST' },
  );
}

export function unmuteTaskNotifications(taskId: string): Promise<void> {
  return fetchVoid(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/notifications/mute`,
    { method: 'DELETE' },
  );
}
