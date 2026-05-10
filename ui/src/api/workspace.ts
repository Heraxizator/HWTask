import type { OrganizationResponse, ProjectResponse } from '../types/task';
import { fetchJson } from './http';

export function listOrganizations(): Promise<OrganizationResponse[]> {
  return fetchJson<OrganizationResponse[]>('/api/v1/organizations');
}

export function listProjects(organizationId: string): Promise<ProjectResponse[]> {
  return fetchJson<ProjectResponse[]>(
    `/api/v1/organizations/${encodeURIComponent(organizationId)}/projects`,
  );
}
