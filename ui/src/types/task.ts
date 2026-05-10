export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export type TaskMemberRole = 'CO_ASSIGNEE' | 'OBSERVER';

export interface TaskMemberEntryResponse {
  userId: string;
  role: TaskMemberRole;
}

export interface TaskResponse {
  id: string;
  projectId: string;
  parentTaskId: string | null;
  assigneeId: string | null;
  createdBy: string | null;
  dueAt: string | null;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority | null;
  createdAt: string;
  updatedAt: string;
  extraMembers: TaskMemberEntryResponse[];
}

export interface CreateTaskRequest {
  projectId: string;
  parentTaskId?: string | null;
  assigneeId?: string | null;
  dueAt?: string | null;
  coAssigneeIds?: string[];
  observerIds?: string[];
  title: string;
  description?: string | null;
  status?: TaskStatus;
  priority?: TaskPriority | null;
}

export interface UpdateTaskRequest {
  title: string;
  description?: string | null;
  status: TaskStatus;
  priority?: TaskPriority | null;
  assigneeId?: string | null;
  dueAt?: string | null;
  coAssigneeIds?: string[] | null;
  observerIds?: string[] | null;
}

/** Spring Data Page JSON (Jackson) */
export interface PageTaskResponse {
  content: TaskResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ProblemDetailBody {
  type?: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}

export interface OrganizationResponse {
  id: string;
  name: string;
  createdAt: string;
}

export interface ProjectResponse {
  id: string;
  organizationId: string;
  name: string;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: {
    id: string;
    email: string;
    displayName: string;
  };
}

export interface CommentResponse {
  id: string;
  authorId: string;
  body: string;
  createdAt: string;
}

export interface ActivityEntryResponse {
  id: string;
  actorId: string | null;
  eventType: string;
  summary: string;
  createdAt: string;
}
