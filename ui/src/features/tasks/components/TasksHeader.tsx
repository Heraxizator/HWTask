import {
  ArchiveRestore,
  Bell,
  KanbanSquare,
  LogOut,
  Plus,
} from 'lucide-react';

import type { NotificationResponse } from '../../../api/notifications';
import {
  Button,
  ButtonColors,
  ButtonSizes,
  ButtonVariants,
} from '../../../portal-ui';
import { formatDt } from '../taskDisplay';

export interface ProjectOption {
  label: string;
  projectId: string;
  organizationId: string;
}

export function TasksHeader({
  currentUserSummary,
  projectOptions,
  projectId,
  onProjectChange,
  onOpenCreate,
  notifOpen,
  onToggleNotifs,
  unreadCount,
  notificationsLoading,
  notificationItems,
  markAllPending,
  onMarkAllRead,
  onNotificationActivate,
  onOpenTrash,
  onLogout,
}: {
  currentUserSummary?: string | null;
  projectOptions: ProjectOption[];
  projectId: string | null;
  onProjectChange: (projectId: string) => void;
  onOpenCreate: () => void;
  notifOpen: boolean;
  onToggleNotifs: () => void;
  unreadCount: number;
  notificationsLoading: boolean;
  notificationItems: NotificationResponse[];
  markAllPending: boolean;
  onMarkAllRead: () => void;
  onNotificationActivate: (n: NotificationResponse) => void;
  onOpenTrash: () => void;
  onLogout: () => void;
}) {
  return (
    <header className="app-header">
      <div className="app-header__row app-header__row--top">
        <div className="app-header__brand">
          <span className="app-header__mark" aria-hidden>
            <KanbanSquare size={22} strokeWidth={2} />
          </span>
          <div className="app-header__brand-text">
            <h1 className="app-title">HWTask</h1>
            <p className="app-sub">Трекер задач в портальном стиле</p>
          </div>
        </div>
        <div className="app-header__user">
          {currentUserSummary ? (
            <span className="app-header__user-name" title={currentUserSummary}>
              {currentUserSummary}
            </span>
          ) : null}
          <Button
            type="button"
            variant={ButtonVariants.GHOST}
            color={ButtonColors.NEUTRAL}
            size={ButtonSizes.MEDIUM}
            className="btn-with-icon app-header__logout"
            onClick={onLogout}
          >
            <LogOut size={18} aria-hidden />
            Выход
          </Button>
        </div>
      </div>

      <div className="app-header__divider" aria-hidden />

      <div className="app-header__row app-header__row--work">
        <div className="app-header__project">
          <label className="app-header__project-label" htmlFor="header-project-select">
            Проект
          </label>
          <select
            id="header-project-select"
            className="app-header__project-select portal-field__input portal-field__input--sm portal-field__select"
            value={projectId ?? ''}
            disabled={!projectOptions.length}
            onChange={(e) => onProjectChange(e.target.value)}
          >
            {!projectOptions.length && <option value="">Нет проектов</option>}
            {projectOptions.map((o) => (
              <option key={o.projectId} value={o.projectId}>
                {o.label}
              </option>
            ))}
          </select>
        </div>
        <div className="app-header__quick">
          <Button
            type="button"
            variant={ButtonVariants.FILLED}
            color={ButtonColors.PRIMARY}
            size={ButtonSizes.MEDIUM}
            disabled={!projectId}
            onClick={onOpenCreate}
            className="btn-with-icon app-header__btn-primary"
          >
            <Plus size={18} strokeWidth={2} aria-hidden />
            Новая задача
          </Button>
          <div className="app-header__dropdown-wrap">
            <Button
              type="button"
              variant={ButtonVariants.GHOST}
              color={ButtonColors.NEUTRAL}
              size={ButtonSizes.MEDIUM}
              aria-expanded={notifOpen}
              aria-haspopup="true"
              onClick={onToggleNotifs}
              disabled={!projectId}
              className="btn-with-icon"
            >
              <Bell size={18} aria-hidden />
              Уведомления
              {unreadCount > 0 && (
                <span className="badge badge-counter" aria-live="polite">
                  {unreadCount > 99 ? '99+' : unreadCount}
                </span>
              )}
            </Button>
            {notifOpen && (
              <div className="notifications-dropdown" role="menu">
                <div className="notifications-dropdown__head">
                  <span className="notifications-dropdown__title">Лента</span>
                  <Button
                    type="button"
                    variant={ButtonVariants.GHOST}
                    color={ButtonColors.NEUTRAL}
                    size={ButtonSizes.SMALL}
                    disabled={markAllPending}
                    onClick={onMarkAllRead}
                  >
                    Прочитать всё
                  </Button>
                </div>
                {notificationsLoading ? (
                  <p className="muted" style={{ margin: '0.5rem 0' }}>
                    Загрузка…
                  </p>
                ) : notificationItems.length === 0 ? (
                  <p className="muted" style={{ margin: '0.75rem 0', textAlign: 'center', fontSize: '0.9rem' }}>
                    Нет уведомлений
                  </p>
                ) : (
                  <ul>
                    {notificationItems.map((n) => (
                      <li
                        key={n.id}
                        className={
                          'notifications-dropdown__item' +
                          (n.read ? ' notifications-dropdown__item--read' : '')
                        }
                      >
                        <Button
                          type="button"
                          variant={ButtonVariants.GHOST}
                          color={ButtonColors.NEUTRAL}
                          size={ButtonSizes.SMALL}
                          className="notifications-dropdown__btn"
                          onClick={() => onNotificationActivate(n)}
                        >
                          <div className="notifications-dropdown__item-title">{n.title}</div>
                          {n.body && (
                            <div className="muted" style={{ fontSize: '0.85rem', marginTop: '0.2rem', lineHeight: 1.4 }}>
                              {n.body}
                            </div>
                          )}
                          <div className="notifications-dropdown__item-meta">{formatDt(n.createdAt)}</div>
                        </Button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </div>
          <Button
            type="button"
            variant={ButtonVariants.GHOST}
            color={ButtonColors.NEUTRAL}
            size={ButtonSizes.MEDIUM}
            className="btn-with-icon"
            onClick={onOpenTrash}
            disabled={!projectId}
            aria-label="Корзина удалённых задач"
          >
            <ArchiveRestore size={18} aria-hidden />
            Корзина
          </Button>
        </div>
      </div>
    </header>
  );
}
