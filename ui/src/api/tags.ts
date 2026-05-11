import { fetchJson, fetchVoid } from './http';

export interface TagResponse {
  id: string;
  name: string;
}

export function listProjectTags(projectId: string): Promise<TagResponse[]> {
  return fetchJson<TagResponse[]>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/tags`,
  );
}

export function createProjectTag(
  projectId: string,
  name: string,
): Promise<TagResponse> {
  return fetchJson<TagResponse>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/tags`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name }),
    },
  );
}

export function setTaskTags(taskId: string, tagIds: string[]): Promise<void> {
  return fetchVoid(
    `/api/v1/tasks/${encodeURIComponent(taskId)}/tags`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tagIds }),
    },
  );
}
