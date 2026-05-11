import type {
  OrganizationResponse,
  ProjectMemberResponse,
  ProjectResponse,
  ProjectRole,
} from '../types/task';
import { fetchJson } from './http';

export function listOrganizations(): Promise<OrganizationResponse[]> {
  return fetchJson<OrganizationResponse[]>('/api/v1/organizations');
}

export function createOrganization(name: string): Promise<OrganizationResponse> {
  return fetchJson<OrganizationResponse>('/api/v1/organizations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  });
}

export function listProjects(organizationId: string): Promise<ProjectResponse[]> {
  return fetchJson<ProjectResponse[]>(
    `/api/v1/organizations/${encodeURIComponent(organizationId)}/projects`,
  );
}

export function createProject(
  organizationId: string,
  name: string,
): Promise<ProjectResponse> {
  return fetchJson<ProjectResponse>(
    `/api/v1/organizations/${encodeURIComponent(organizationId)}/projects`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name }),
    },
  );
}

export function listProjectMembers(projectId: string): Promise<ProjectMemberResponse[]> {
  return fetchJson<ProjectMemberResponse[]>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/members`,
  );
}

export function addProjectMember(
  projectId: string,
  body: { userId: string; role: ProjectRole },
): Promise<ProjectMemberResponse> {
  return fetchJson<ProjectMemberResponse>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/members`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    },
  );
}
