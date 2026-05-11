import { fetchJson } from './http';

export interface TimeEntryResponse {
  id: string;
  taskId: string;
  userId: string;
  startedAt: string;
  endedAt: string | null;
  commentNote: string | null;
  durationSeconds: number | null;
}

export function startTimeTracking(
  taskId: string,
  commentNote?: string | null,
): Promise<TimeEntryResponse> {
  return fetchJson<TimeEntryResponse>(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/time-entries/start`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(
        commentNote ? { commentNote } : {},
      ),
    },
  );
}

export function stopTimeTracking(): Promise<TimeEntryResponse> {
  return fetchJson<TimeEntryResponse>('/api/v1/me/time-entries/stop', {
    method: 'POST',
  });
}

export function listTaskTimeEntries(taskId: string): Promise<TimeEntryResponse[]> {
  return fetchJson<TimeEntryResponse[]>(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/time-entries`,
  );
}
