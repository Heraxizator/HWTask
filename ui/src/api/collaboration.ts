import type { ActivityEntryResponse, CommentResponse } from '../types/task';
import { fetchJson } from './http';

export function listComments(taskId: string): Promise<CommentResponse[]> {
  return fetchJson<CommentResponse[]>(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/comments`,
  );
}

export function postComment(taskId: string, body: string): Promise<CommentResponse> {
  return fetchJson<CommentResponse>(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/comments`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ body }),
    },
  );
}

export function listActivity(taskId: string): Promise<ActivityEntryResponse[]> {
  return fetchJson<ActivityEntryResponse[]>(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/activity`,
  );
}
