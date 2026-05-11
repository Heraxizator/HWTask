import { describe, expect, it } from 'vitest';
import {
  formatDt,
  formatDurationSeconds,
  priorityLabel,
  statusBadgeClass,
  statusLabel,
  toDatetimeLocalValue,
} from './taskDisplay';

describe('taskDisplay', () => {
  it('statusLabel maps known statuses', () => {
    expect(statusLabel('TODO')).toBe('К выполнению');
    expect(statusLabel('IN_PROGRESS')).toBe('В работе');
    expect(statusLabel('DONE')).toBe('Готово');
  });

  it('statusBadgeClass returns Tailwind-ish class tokens', () => {
    expect(statusBadgeClass('TODO')).toContain('badge-todo');
    expect(statusBadgeClass('IN_PROGRESS')).toContain('badge-progress');
    expect(statusBadgeClass('DONE')).toContain('badge-done');
  });

  it('priorityLabel handles null', () => {
    expect(priorityLabel(null)).toBe('—');
  });

  it('priorityLabel maps priorities', () => {
    expect(priorityLabel('LOW')).toBe('Низкий');
    expect(priorityLabel('MEDIUM')).toBe('Средний');
    expect(priorityLabel('HIGH')).toBe('Высокий');
  });

  it('formatDt formats UTC instant', () => {
    const out = formatDt('2025-06-01T14:30:00.000Z');
    expect(out.length).toBeGreaterThan(0);
    expect(out).not.toBe('2025-06-01T14:30:00.000Z');
  });

  it('formatDt returns raw string on invalid input', () => {
    expect(formatDt('not-a-date')).toBe('not-a-date');
  });

  it('formatDurationSeconds handles null and negatives', () => {
    expect(formatDurationSeconds(null)).toBe('—');
    expect(formatDurationSeconds(-1)).toBe('—');
  });

  it('formatDurationSeconds formats hours minutes seconds', () => {
    expect(formatDurationSeconds(3661)).toBe('1ч 1м');
    expect(formatDurationSeconds(125)).toBe('2м 5с');
    expect(formatDurationSeconds(7)).toBe('7с');
  });

  it('toDatetimeLocalValue returns empty for null', () => {
    expect(toDatetimeLocalValue(null)).toBe('');
  });

  it('toDatetimeLocalValue builds local datetime-local fragment', () => {
    const v = toDatetimeLocalValue('2025-03-09T08:05:00.000Z');
    expect(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(v)).toBe(true);
  });
});
