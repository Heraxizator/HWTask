import type { TaskTrashEntryResponse } from '../../../api/trash';

export function TrashModal({
  loading,
  items,
  restorePending,
  purgePending,
  onClose,
  onRestore,
  onPurge,
}: {
  loading: boolean;
  items: TaskTrashEntryResponse[];
  restorePending: boolean;
  purgePending: boolean;
  onClose: () => void;
  onRestore: (taskId: string) => void;
  onPurge: (taskId: string) => void;
}) {
  return (
    <div
      className="modal-backdrop"
      role="presentation"
      onClick={(ev) => {
        if (ev.target === ev.currentTarget) onClose();
      }}
    >
      <div
        className="modal panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="trash-title"
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: 520, width: '100%' }}
      >
        <h2 id="trash-title" style={{ marginTop: 0 }}>Корзина</h2>
        <p className="muted" style={{ marginTop: 0 }}>
          Удалённые задачи проекта. Восстановление вернёт задачу в список.
        </p>
        {loading ? (
          <p className="muted">Загрузка…</p>
        ) : items.length === 0 ? (
          <p className="muted" style={{ marginBottom: '1rem' }}>Корзина пуста</p>
        ) : (
          <ul style={{ listStyle: 'none', padding: 0, margin: '0 0 1rem 0', maxHeight: 280, overflow: 'auto' }}>
            {items.map((row) => (
              <li
                key={row.id}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: '0.75rem',
                  padding: '0.5rem 0',
                  borderBottom: '1px solid var(--color-border)',
                  fontSize: '0.9rem',
                }}
              >
                <span style={{ flex: 1, minWidth: 0 }}>{row.title}</span>
                <div style={{ display: 'flex', flexShrink: 0, gap: '0.35rem' }}>
                  <button
                    type="button"
                    className="btn btn-primary"
                    style={{ fontSize: '0.85rem', padding: '0.25rem 0.5rem' }}
                    disabled={restorePending}
                    onClick={() => onRestore(row.id)}
                  >
                    Восстановить
                  </button>
                  <button
                    type="button"
                    className="btn btn-danger"
                    style={{ fontSize: '0.85rem', padding: '0.25rem 0.5rem' }}
                    disabled={purgePending}
                    onClick={() => onPurge(row.id)}
                  >
                    Удалить навсегда
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
        <div className="modal-actions" style={{ marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Закрыть
          </button>
        </div>
      </div>
    </div>
  );
}
