export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export type TaskMemberRole = 'CO_ASSIGNEE' | 'OBSERVER';

export interface TaskMemberEntryResponse {
  userId: string;
  role: TaskMemberRole;
}

export interface TaskTagResponse {
  id: string;
  name: string;
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
  tags: TaskTagResponse[];
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
  tagIds?: string[];
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

export interface UserPublicResponse {
  id: string;
  email: string;
  displayName: string;
}

export type ProjectRole = 'MANAGER' | 'MEMBER';

export interface ProjectMemberResponse {
  userId: string;
  email: string;
  displayName: string;
  role: ProjectRole;
}

export interface TaskSummaryReportResponse {
  total: number;
  todo: number;
  inProgress: number;
  done: number;
  overdue: number;
}

export interface UserTimeShareResponse {
  userId: string;
  seconds: number;
}

export interface ProjectTimeSummaryResponse {
  totalSeconds: number;
  byUser: UserTimeShareResponse[];
}

export type RuleTriggerType = 'ON_STATUS_CHANGE' | 'ON_TASK_OVERDUE';
export type RuleActionType = 'NOTIFY_ASSIGNEE' | 'ADD_ACTIVITY_NOTE';

export interface AutomationRuleResponse {
  id: string;
  projectId: string;
  triggerType: RuleTriggerType;
  actionType: RuleActionType;
  enabled: boolean;
  createdAt: string;
}

export interface CreateAutomationRuleRequest {
  triggerType: RuleTriggerType;
  actionType: RuleActionType;
  enabled: boolean;
}

export interface AttachmentResponse {
  id: string;
  uploadedBy: string;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  createdAt: string;
}

export interface ReminderResponse {
  id: string;
  taskId: string;
  userId: string;
  remindAt: string;
  firedAt: string | null;
}
