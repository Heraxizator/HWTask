import type {
  ActivityEntryResponse,
  AttachmentResponse,
  CommentResponse,
  ReminderResponse,
} from '../types/task';
import { fetchBlob, fetchJson, fetchJsonMultipart } from './http';

const taskBase = (taskId: string) => `/api/v1/tasks/${encodeURIComponent(taskId)}`;

export function listComments(taskId: string): Promise<CommentResponse[]> {
  return fetchJson<CommentResponse[]>(`${taskBase(taskId)}/comments`);
}

export function postComment(taskId: string, body: string): Promise<CommentResponse> {
  return fetchJson<CommentResponse>(`${taskBase(taskId)}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ body }),
  });
}

export function listActivity(taskId: string): Promise<ActivityEntryResponse[]> {
  return fetchJson<ActivityEntryResponse[]>(`${taskBase(taskId)}/activity`);
}

export function listAttachments(taskId: string): Promise<AttachmentResponse[]> {
  return fetchJson<AttachmentResponse[]>(`${taskBase(taskId)}/attachments`);
}

export function uploadAttachment(
  taskId: string,
  file: File,
): Promise<AttachmentResponse> {
  const fd = new FormData();
  fd.set('file', file);
  return fetchJsonMultipart<AttachmentResponse>(`${taskBase(taskId)}/attachments`, fd);
}

export function downloadAttachmentFile(taskId: string, attachmentId: string): Promise<Blob> {
  return fetchBlob(`${taskBase(taskId)}/attachments/${encodeURIComponent(attachmentId)}/file`);
}

export function listReminders(taskId: string): Promise<ReminderResponse[]> {
  return fetchJson<ReminderResponse[]>(`${taskBase(taskId)}/reminders`);
}

export function createReminder(
  taskId: string,
  remindAtIso: string,
): Promise<ReminderResponse> {
  return fetchJson<ReminderResponse>(`${taskBase(taskId)}/reminders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ remindAt: remindAtIso }),
  });
}
