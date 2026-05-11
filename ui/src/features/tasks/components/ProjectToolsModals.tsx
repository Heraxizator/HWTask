import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';

import {
  createAutomationRule,
  deleteAutomationRule,
  listAutomationRules,
} from '../../../api/automation';
import { getProjectExtendedStats, getProjectTimeSummary, getTaskSummary } from '../../../api/reports';
import { ApiError } from '../../../api/http';
import type { OrganizationResponse } from '../../../types/task';
import {
  addProjectMember,
  createOrganization,
  createProject,
  listProjectMembers,
} from '../../../api/workspace';
import type {
  CreateAutomationRuleRequest,
  ProjectRole,
  RuleActionType,
  RuleTriggerType,
} from '../../../types/task';
import {
  Button,
  ButtonColors,
  ButtonSizes,
  ButtonVariants,
} from '../../../portal-ui';
import { formatDurationSeconds } from '../taskDisplay';

function shortDate(isoDay: string): string {
  // isoDay = yyyy-mm-dd
  const [, m, d] = isoDay.split('-');
  return `${d}.${m}`;
}

function spark(values: number[], width = 140, height = 26): string {
  if (!values.length) return '';
  const max = Math.max(1, ...values);
  const step = values.length === 1 ? 0 : width / (values.length - 1);
  return values
    .map((v, i) => {
      const x = i * step;
      const y = height - (v / max) * (height - 2);
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(' ');
}

export function ReportsModal({
  open,
  projectId,
  onClose,
}: {
  open: boolean;
  projectId: string | null;
  onClose: () => void;
}) {
  const tasksQ = useQuery({
    queryKey: ['reports', 'tasks-summary', projectId],
    queryFn: () => getTaskSummary(projectId!),
    enabled: open && !!projectId,
  });
  const timeQ = useQuery({
    queryKey: ['reports', 'time-summary', projectId],
    queryFn: () => getProjectTimeSummary(projectId!),
    enabled: open && !!projectId,
  });
  const extQ = useQuery({
    queryKey: ['reports', 'extended', projectId],
    queryFn: () => getProjectExtendedStats(projectId!),
    enabled: open && !!projectId,
  });

  if (!open) return null;

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(ev) => {
        if (ev.target === ev.currentTarget) onClose();
      }}
    >
      <div
        className="modal modal--wide panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="reports-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="reports-title">Сводки по проекту</h2>
        {!projectId ? (
          <p className="muted">Выберите проект.</p>
        ) : (
          <div style={{ display: 'grid', gap: '1.25rem' }}>
            <section>
              <h3 className="subsection-head">Задачи</h3>
              {tasksQ.isLoading ? (
                <p className="muted">Загрузка…</p>
              ) : tasksQ.error ? (
                <p className="alert" role="alert">
                  {tasksQ.error instanceof Error ? tasksQ.error.message : 'Ошибка'}
                </p>
              ) : (
                <dl className="report-dl">
                  <div>
                    <dt>Всего</dt>
                    <dd>{tasksQ.data?.total}</dd>
                  </div>
                  <div>
                    <dt>К выполнению</dt>
                    <dd>{tasksQ.data?.todo}</dd>
                  </div>
                  <div>
                    <dt>В работе</dt>
                    <dd>{tasksQ.data?.inProgress}</dd>
                  </div>
                  <div>
                    <dt>Готово</dt>
                    <dd>{tasksQ.data?.done}</dd>
                  </div>
                  <div>
                    <dt>Просрочено</dt>
                    <dd>{tasksQ.data?.overdue}</dd>
                  </div>
                </dl>
              )}
            </section>
            <section>
              <h3 className="subsection-head">Учёт времени</h3>
              {timeQ.isLoading ? (
                <p className="muted">Загрузка…</p>
              ) : timeQ.error ? (
                <p className="alert" role="alert">
                  {timeQ.error instanceof Error ? timeQ.error.message : 'Ошибка'}
                </p>
              ) : (
                <>
                  <p className="muted" style={{ marginTop: 0 }}>
                    Всего:{' '}
                    <strong>{formatDurationSeconds(timeQ.data?.totalSeconds ?? 0)}</strong>
                  </p>
                  <ul style={{ margin: 0, paddingLeft: '1.1rem' }}>
                    {(timeQ.data?.byUser ?? []).map((u) => (
                      <li key={u.userId} className="muted" style={{ marginBottom: '0.35rem' }}>
                        <code style={{ fontSize: '0.85em' }}>{u.userId}</code>:{' '}
                        {formatDurationSeconds(u.seconds)}
                      </li>
                    ))}
                  </ul>
                  {!(timeQ.data?.byUser?.length) && (
                    <p className="muted" style={{ marginBottom: 0 }}>
                      Нет распределения по пользователям.
                    </p>
                  )}
                </>
              )}
            </section>
            <section>
              <h3 className="subsection-head">Динамика (30 дней)</h3>
              {extQ.isLoading ? (
                <p className="muted">Загрузка…</p>
              ) : extQ.error ? (
                <p className="alert" role="alert">
                  {extQ.error instanceof Error ? extQ.error.message : 'Ошибка'}
                </p>
              ) : (
                <div style={{ display: 'grid', gap: '0.85rem' }}>
                  <div className="detail-well" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem' }}>
                    <div>
                      <div style={{ fontWeight: 700, color: 'var(--color-text-01)' }}>Создано</div>
                      <div className="muted" style={{ fontSize: '0.85rem' }}>
                        {extQ.data?.throughputCreated?.length ? `${shortDate(extQ.data.throughputCreated[0].day)} → ${shortDate(extQ.data.throughputCreated[extQ.data.throughputCreated.length - 1].day)}` : '—'}
                      </div>
                    </div>
                    <svg width="140" height="26" viewBox="0 0 140 26" aria-hidden>
                      <polyline
                        fill="none"
                        stroke="var(--color-primary-main)"
                        strokeWidth="2"
                        points={spark((extQ.data?.throughputCreated ?? []).map((x) => x.count))}
                      />
                    </svg>
                  </div>

                  <div className="detail-well" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem' }}>
                    <div>
                      <div style={{ fontWeight: 700, color: 'var(--color-text-01)' }}>Закрыто</div>
                      <div className="muted" style={{ fontSize: '0.85rem' }}>
                        Среднее время до DONE: <strong>{extQ.data?.avgLeadTimeHours ?? 0}ч</strong>
                      </div>
                    </div>
                    <svg width="140" height="26" viewBox="0 0 140 26" aria-hidden>
                      <polyline
                        fill="none"
                        stroke="var(--color-text-01)"
                        strokeOpacity="0.65"
                        strokeWidth="2"
                        points={spark((extQ.data?.throughputDone ?? []).map((x) => x.count))}
                      />
                    </svg>
                  </div>
                </div>
              )}
            </section>
          </div>
        )}
        <div className="modal-actions" style={{ marginTop: '1rem' }}>
          <Button
            type="button"
            variant={ButtonVariants.GHOST}
            color={ButtonColors.NEUTRAL}
            size={ButtonSizes.MEDIUM}
            onClick={onClose}
          >
            Закрыть
          </Button>
        </div>
      </div>
    </div>
  );
}

const TRIGGER_OPTIONS: { value: RuleTriggerType; label: string }[] = [
  { value: 'ON_STATUS_CHANGE', label: 'Смена статуса' },
  { value: 'ON_TASK_OVERDUE', label: 'Просрочка задачи' },
];

const ACTION_OPTIONS: { value: RuleActionType; label: string }[] = [
  { value: 'NOTIFY_ASSIGNEE', label: 'Уведомить исполнителя' },
  { value: 'ADD_ACTIVITY_NOTE', label: 'Заметка в активности' },
];

export function AutomationRulesModal({
  open,
  projectId,
  onClose,
}: {
  open: boolean;
  projectId: string | null;
  onClose: () => void;
}) {
  const queryClient = useQueryClient();
  const listQ = useQuery({
    queryKey: ['automation-rules', projectId],
    queryFn: () => listAutomationRules(projectId!),
    enabled: open && !!projectId,
  });

  const [triggerType, setTriggerType] = useState<RuleTriggerType>('ON_STATUS_CHANGE');
  const [actionType, setActionType] = useState<RuleActionType>('NOTIFY_ASSIGNEE');
  const [enabled, setEnabled] = useState(true);
  const [formErr, setFormErr] = useState<string | null>(null);

  const createMut = useMutation({
    mutationFn: (body: CreateAutomationRuleRequest) =>
      createAutomationRule(projectId!, body),
    onSuccess: () => {
      setFormErr(null);
      void queryClient.invalidateQueries({ queryKey: ['automation-rules', projectId] });
    },
    onError: (e: Error) => {
      setFormErr(e instanceof ApiError ? e.message : e.message || 'Ошибка');
    },
  });

  const delMut = useMutation({
    mutationFn: (ruleId: string) => deleteAutomationRule(projectId!, ruleId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['automation-rules', projectId] });
    },
  });

  if (!open) return null;

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(ev) => {
        if (ev.target === ev.currentTarget) onClose();
      }}
    >
      <div
        className="modal modal--wide panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="auto-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="auto-title">Правила автоматизации</h2>
        {!projectId ? (
          <p className="muted">Выберите проект.</p>
        ) : (
          <>
            {listQ.isLoading ? (
              <p className="muted">Загрузка…</p>
            ) : listQ.error ? (
              <p className="alert" role="alert">
                {listQ.error instanceof Error ? listQ.error.message : 'Ошибка'}
              </p>
            ) : (
              <ul style={{ listStyle: 'none', margin: 0, padding: 0 }}>
                {(listQ.data ?? []).map((r) => (
                  <li
                    key={r.id}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.75rem',
                      padding: '0.5rem 0',
                      borderBottom: '1px solid var(--color-border-soft)',
                    }}
                  >
                    <span className="muted" style={{ flex: 1, fontSize: '0.9rem' }}>
                      {r.triggerType} → {r.actionType}
                      {r.enabled ? '' : ' (выкл.)'}
                    </span>
                    <Button
                      type="button"
                      variant={ButtonVariants.FILLED}
                      color={ButtonColors.DANGER}
                      size={ButtonSizes.SMALL}
                      disabled={delMut.isPending}
                      onClick={() => delMut.mutate(r.id)}
                    >
                      Удалить
                    </Button>
                  </li>
                ))}
              </ul>
            )}
            <h3 className="subsection-head" style={{ marginTop: '1.25rem' }}>
              Новое правило
            </h3>
            {formErr && (
              <div className="alert" role="alert">
                {formErr}
              </div>
            )}
            <div className="field">
              <label htmlFor="auto-trigger">Триггер</label>
              <select
                id="auto-trigger"
                value={triggerType}
                onChange={(e) => setTriggerType(e.target.value as RuleTriggerType)}
              >
                {TRIGGER_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label htmlFor="auto-action">Действие</label>
              <select
                id="auto-action"
                value={actionType}
                onChange={(e) => setActionType(e.target.value as RuleActionType)}
              >
                {ACTION_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </div>
            <label className="tag-assign-pill" style={{ display: 'inline-flex', marginBottom: '0.75rem' }}>
              <input
                type="checkbox"
                checked={enabled}
                onChange={(e) => setEnabled(e.target.checked)}
              />
              Включено
            </label>
            <div>
              <Button
                type="button"
                variant={ButtonVariants.FILLED}
                color={ButtonColors.PRIMARY}
                size={ButtonSizes.MEDIUM}
                loading={createMut.isPending}
                onClick={() =>
                  createMut.mutate({ triggerType, actionType, enabled })
                }
              >
                Добавить правило
              </Button>
            </div>
          </>
        )}
        <div className="modal-actions" style={{ marginTop: '1rem' }}>
          <Button
            type="button"
            variant={ButtonVariants.GHOST}
            color={ButtonColors.NEUTRAL}
            size={ButtonSizes.MEDIUM}
            onClick={onClose}
          >
            Закрыть
          </Button>
        </div>
      </div>
    </div>
  );
}

export function MembersModal({
  open,
  projectId,
  onClose,
}: {
  open: boolean;
  projectId: string | null;
  onClose: () => void;
}) {
  const queryClient = useQueryClient();
  const listQ = useQuery({
    queryKey: ['project-members', projectId],
    queryFn: () => listProjectMembers(projectId!),
    enabled: open && !!projectId,
  });

  const [userId, setUserId] = useState('');
  const [role, setRole] = useState<ProjectRole>('MEMBER');
  const [formErr, setFormErr] = useState<string | null>(null);

  const addMut = useMutation({
    mutationFn: () =>
      addProjectMember(projectId!, { userId: userId.trim(), role }),
    onSuccess: () => {
      setUserId('');
      setFormErr(null);
      void queryClient.invalidateQueries({ queryKey: ['project-members', projectId] });
    },
    onError: (e: Error) => {
      setFormErr(e instanceof ApiError ? e.message : e.message || 'Ошибка');
    },
  });

  if (!open) return null;

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(ev) => {
        if (ev.target === ev.currentTarget) onClose();
      }}
    >
      <div
        className="modal modal--wide panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="members-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="members-title">Участники проекта</h2>
        {!projectId ? (
          <p className="muted">Выберите проект.</p>
        ) : (
          <>
            <div className="modal-form__scroll" style={{ paddingBottom: '0.5rem' }}>
              {listQ.isLoading ? (
                <p className="muted">Загрузка…</p>
              ) : listQ.error ? (
                <p className="alert" role="alert">
                  {listQ.error instanceof Error ? listQ.error.message : 'Ошибка'}
                </p>
              ) : (
                <ul style={{ margin: '0 0 1rem', paddingLeft: '1.1rem' }}>
                  {(listQ.data ?? []).map((m) => (
                    <li key={m.userId} className="muted" style={{ marginBottom: '0.35rem' }}>
                      <strong>{m.displayName || m.email}</strong> ({m.role})
                      <div style={{ fontSize: '0.82rem' }}>{m.userId}</div>
                    </li>
                  ))}
                </ul>
              )}
              <h3 className="subsection-head">Добавить участника</h3>
              {formErr && (
                <div className="alert" role="alert">
                  {formErr}
                </div>
              )}
              <div className="field">
                <label htmlFor="member-user-id">UUID пользователя</label>
                <input
                  id="member-user-id"
                  value={userId}
                  onChange={(e) => setUserId(e.target.value)}
                  placeholder="00000000-0000-0000-0000-000000000000"
                  autoComplete="off"
                />
              </div>
              <div className="field">
                <label htmlFor="member-role">Роль</label>
                <select
                  id="member-role"
                  value={role}
                  onChange={(e) => setRole(e.target.value as ProjectRole)}
                >
                  <option value="MEMBER">Участник</option>
                  <option value="MANAGER">Менеджер</option>
                </select>
              </div>
            </div>
          </>
        )}
        <div className="modal-form__footer">
          <div className="modal-actions">
            {projectId ? (
              <Button
                type="button"
                variant={ButtonVariants.FILLED}
                color={ButtonColors.PRIMARY}
                size={ButtonSizes.MEDIUM}
                loading={addMut.isPending}
                disabled={!userId.trim()}
                onClick={() => addMut.mutate()}
              >
                Добавить
              </Button>
            ) : null}
            <Button
              type="button"
              variant={ButtonVariants.GHOST}
              color={ButtonColors.NEUTRAL}
              size={ButtonSizes.MEDIUM}
              onClick={onClose}
            >
              Закрыть
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

export function WorkspaceModal({
  open,
  organizations,
  onClose,
  onChanged,
}: {
  open: boolean;
  organizations: OrganizationResponse[];
  onClose: () => void;
  onChanged: () => void;
}) {
  const queryClient = useQueryClient();
  const [orgName, setOrgName] = useState('');
  const [projOrgId, setProjOrgId] = useState('');
  const [projName, setProjName] = useState('');
  const [errOrg, setErrOrg] = useState<string | null>(null);
  const [errProj, setErrProj] = useState<string | null>(null);

  const createOrgMut = useMutation({
    mutationFn: () => createOrganization(orgName.trim()),
    onSuccess: () => {
      setOrgName('');
      setErrOrg(null);
      void queryClient.invalidateQueries({ queryKey: ['workspace'] });
      onChanged();
    },
    onError: (e: Error) => {
      setErrOrg(e instanceof ApiError ? e.message : e.message || 'Ошибка');
    },
  });

  useEffect(() => {
    if (open && organizations.length) {
      setProjOrgId((prev) => prev || organizations[0].id);
    }
  }, [open, organizations]);

  const createProjMut = useMutation({
    mutationFn: () => createProject(projOrgId, projName.trim()),
    onSuccess: () => {
      setProjName('');
      setErrProj(null);
      void queryClient.invalidateQueries({ queryKey: ['workspace'] });
      onChanged();
    },
    onError: (e: Error) => {
      setErrProj(e instanceof ApiError ? e.message : e.message || 'Ошибка');
    },
  });

  if (!open) return null;

  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(ev) => {
        if (ev.target === ev.currentTarget) onClose();
      }}
    >
      <div
        className="modal modal--wide panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="ws-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="ws-title">Организации и проекты</h2>
        <section style={{ marginBottom: '1.5rem' }}>
          <h3 className="subsection-head">Новая организация</h3>
          {errOrg && (
            <div className="alert" role="alert">
              {errOrg}
            </div>
          )}
          <div className="field">
            <label htmlFor="new-org-name">Название</label>
            <input
              id="new-org-name"
              value={orgName}
              onChange={(e) => setOrgName(e.target.value)}
              maxLength={255}
            />
          </div>
          <Button
            type="button"
            variant={ButtonVariants.FILLED}
            color={ButtonColors.PRIMARY}
            size={ButtonSizes.MEDIUM}
            loading={createOrgMut.isPending}
            disabled={!orgName.trim()}
            onClick={() => createOrgMut.mutate()}
          >
            Создать организацию
          </Button>
        </section>
        <section>
          <h3 className="subsection-head">Новый проект</h3>
          {errProj && (
            <div className="alert" role="alert">
              {errProj}
            </div>
          )}
          <div className="field">
            <label htmlFor="ws-org">Организация</label>
            <select
              id="ws-org"
              value={projOrgId}
              onChange={(e) => setProjOrgId(e.target.value)}
            >
              <option value="">— выберите —</option>
              {organizations.map((o) => (
                <option key={o.id} value={o.id}>
                  {o.name}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="new-proj-name">Название проекта</label>
            <input
              id="new-proj-name"
              value={projName}
              onChange={(e) => setProjName(e.target.value)}
              maxLength={255}
            />
          </div>
          <Button
            type="button"
            variant={ButtonVariants.FILLED}
            color={ButtonColors.PRIMARY}
            size={ButtonSizes.MEDIUM}
            loading={createProjMut.isPending}
            disabled={!projOrgId || !projName.trim()}
            onClick={() => createProjMut.mutate()}
          >
            Создать проект
          </Button>
        </section>
        <div className="modal-actions" style={{ marginTop: '1rem' }}>
          <Button
            type="button"
            variant={ButtonVariants.GHOST}
            color={ButtonColors.NEUTRAL}
            size={ButtonSizes.MEDIUM}
            onClick={onClose}
          >
            Закрыть
          </Button>
        </div>
      </div>
    </div>
  );
}
