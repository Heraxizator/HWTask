import type {
  AutomationRuleResponse,
  CreateAutomationRuleRequest,
} from '../types/task';
import { fetchJson, fetchVoid } from './http';

export function listAutomationRules(
  projectId: string,
): Promise<AutomationRuleResponse[]> {
  return fetchJson<AutomationRuleResponse[]>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/automation-rules`,
  );
}

export function createAutomationRule(
  projectId: string,
  body: CreateAutomationRuleRequest,
): Promise<AutomationRuleResponse> {
  return fetchJson<AutomationRuleResponse>(
    `/api/v1/projects/${encodeURIComponent(projectId)}/automation-rules`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    },
  );
}

export function deleteAutomationRule(projectId: string, ruleId: string): Promise<void> {
  return fetchVoid(
    `/api/v1/projects/${encodeURIComponent(projectId)}/automation-rules/${encodeURIComponent(ruleId)}`,
    { method: 'DELETE' },
  );
}
