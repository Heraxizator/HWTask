import type {
  ProjectTimeSummaryResponse,
  TaskSummaryReportResponse,
} from '../types/task';
import { fetchJson } from './http';

export function getTaskSummary(projectId: string): Promise<TaskSummaryReportResponse> {
  return fetchJson<TaskSummaryReportResponse>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/reports/tasks-summary`,
  );
}

export function getProjectTimeSummary(
  projectId: string,
): Promise<ProjectTimeSummaryResponse> {
  return fetchJson<ProjectTimeSummaryResponse>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/reports/time-summary`,
  );
}
