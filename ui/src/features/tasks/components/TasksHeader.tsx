import {
  ArchiveRestore,
  Bell,
  LogOut,
  Plus,
} from 'lucide-react';

import type { NotificationResponse } from '../../../api/notifications';
import { formatDt } from '../taskDisplay';

export interface ProjectOption {
  label: string;
  projectId: string;
}

export function TasksHeader({
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
      <div>
        <h1 className="app-title">HWTask</h1>
        <p className="app-sub">Проекты, роли и лента как в привычном портале</p>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <label className="muted" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          Проект
          <select
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
        </label>
        <button type="button" className="btn btn-primary" onClick={onOpenCreate} disabled={!projectId}>
          <Plus size={18} strokeWidth={2} style={{ verticalAlign: 'middle', marginRight: '0.35rem' }} aria-hidden />
          Новая задача
        </button>
        <div style={{ position: 'relative' }}>
          <button
            type="button"
            className="btn btn-ghost"
            aria-expanded={notifOpen}
            aria-haspopup="true"
            onClick={onToggleNotifs}
            disabled={!projectId}
          >
            <Bell size={18} style={{ marginRight: '0.35rem' }} aria-hidden />
            Уведомления
            {unreadCount > 0 && (
              <span className="badge badge-progress" style={{ marginLeft: '0.35rem', fontSize: '0.75rem' }}>
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>
          {notifOpen && (
            <div
              className="panel"
              role="menu"
              style={{
                position: 'absolute',
                right: 0,
                top: '100%',
                marginTop: '0.5rem',
                zIndex: 40,
                minWidth: 320,
                maxWidth: 420,
                maxHeight: 360,
                overflow: 'auto',
                padding: '0.75rem',
                boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem', gap: '0.5rem' }}>
                <strong style={{ fontSize: '0.9rem' }}>Лента</strong>
                <button
                  type="button"
                  className="btn btn-ghost"
                  style={{ fontSize: '0.85rem', padding: '0.25rem 0.5rem' }}
                  disabled={markAllPending}
                  onClick={onMarkAllRead}
                >
                  Прочитать всё
                </button>
              </div>
              {notificationsLoading ? (
                <p className="muted" style={{ margin: 0 }}>Загрузка…</p>
              ) : notificationItems.length === 0 ? (
                <p className="muted" style={{ margin: 0 }}>Нет уведомлений</p>
              ) : (
                <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                  {notificationItems.map((n) => (
                    <li
                      key={n.id}
                      style={{
                        padding: '0.5rem 0',
                        borderBottom: '1px solid var(--color-border)',
                        opacity: n.read ? 0.75 : 1,
                      }}
                    >
                      <button
                        type="button"
                        className="btn btn-ghost"
                        style={{
                          width: '100%',
                          textAlign: 'left',
                          padding: 0,
                          height: 'auto',
                          whiteSpace: 'normal',
                        }}
                        onClick={() => onNotificationActivate(n)}
                      >
                        <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{n.title}</div>
                        {n.body && (
                          <div className="muted" style={{ fontSize: '0.85rem', marginTop: '0.2rem' }}>
                            {n.body}
                          </div>
                        )}
                        <div className="muted" style={{ fontSize: '0.75rem', marginTop: '0.25rem' }}>
                          {formatDt(n.createdAt)}
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>
        <button
          type="button"
          className="btn btn-ghost"
          onClick={onOpenTrash}
          disabled={!projectId}
          aria-label="Корзина удалённых задач"
        >
          <ArchiveRestore size={18} style={{ marginRight: '0.35rem' }} aria-hidden />
          Корзина
        </button>
        <button type="button" className="btn btn-ghost" onClick={onLogout}>
          <LogOut size={18} style={{ marginRight: '0.35rem' }} aria-hidden />
          Выход
        </button>
      </div>
    </header>
  );
}
