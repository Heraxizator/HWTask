import type {
  ProjectTimeSummaryResponse,
  TaskSummaryReportResponse,
} from '../types/task';
import { fetchJson } from './http';

export interface DayCount {
  day: string; // yyyy-mm-dd
  count: number;
}

export interface DaySeconds {
  day: string; // yyyy-mm-dd
  seconds: number;
}

export interface UserSeconds {
  userId: string;
  seconds: number;
}

export interface ActivityDayCount {
  day: string; // yyyy-mm-dd
  type: string;
  count: number;
}

export interface ProjectExtendedStatsResponse {
  tasks: TaskSummaryReportResponse;
  avgLeadTimeHours: number;
  throughputCreated: DayCount[];
  throughputDone: DayCount[];
  timeByDaySeconds: DaySeconds[];
  timeByUserSeconds: UserSeconds[];
  activityByDay: ActivityDayCount[];
}

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

export function getProjectExtendedStats(
  projectId: string,
  params?: { from?: string; to?: string },
): Promise<ProjectExtendedStatsResponse> {
  const sp = new URLSearchParams();
  if (params?.from) sp.set('from', params.from);
  if (params?.to) sp.set('to', params.to);
  const qs = sp.toString();
  return fetchJson<ProjectExtendedStatsResponse>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/reports/extended${qs ? `?${qs}` : ''}`,
  );
}
