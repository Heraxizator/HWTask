import type { TaskPriority, TaskStatus } from '../../types/task';

export const STATUS_OPTIONS: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

export const PRIORITY_OPTIONS: (TaskPriority | '')[] = ['', 'LOW', 'MEDIUM', 'HIGH'];

export function formatDt(iso: string): string {
  try {
    return new Intl.DateTimeFormat('ru-RU', {
      dateStyle: 'short',
      timeStyle: 'short',
      timeZone: 'UTC',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

export function statusBadgeClass(s: TaskStatus): string {
  if (s === 'DONE') return 'badge badge-done';
  if (s === 'IN_PROGRESS') return 'badge badge-progress';
  return 'badge badge-todo';
}

export function statusLabel(s: TaskStatus): string {
  if (s === 'TODO') return 'К выполнению';
  if (s === 'IN_PROGRESS') return 'В работе';
  return 'Готово';
}

export function priorityLabel(p: TaskPriority | null): string {
  if (!p) return '—';
  if (p === 'LOW') return 'Низкий';
  if (p === 'MEDIUM') return 'Средний';
  return 'Высокий';
}

export function formatDurationSeconds(sec: number | null): string {
  if (sec == null || sec < 0) return '—';
  const h = Math.floor(sec / 3600);
  const m = Math.floor((sec % 3600) / 60);
  const s = sec % 60;
  if (h > 0) return `${h}ч ${m}м`;
  if (m > 0) return `${m}м ${s}с`;
  return `${s}с`;
}

export function toDatetimeLocalValue(iso: string | null): string {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
