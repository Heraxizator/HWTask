import type { TaskTrashEntryResponse } from '../../../api/trash';
import {
  Button,
  ButtonColors,
  ButtonSizes,
  ButtonVariants,
} from '../../../portal-ui';

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
        className="modal modal--wide panel"
        role="dialog"
        aria-modal="true"
        aria-labelledby="trash-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="trash-title">Корзина</h2>
        <p className="muted" style={{ marginTop: 0, lineHeight: 1.5 }}>
          Удалённые задачи проекта. Восстановление вернёт задачу в список активных задач.
        </p>
        {loading ? (
          <p className="muted">Загрузка…</p>
        ) : items.length === 0 ? (
          <div className="state-block state-block--rich" style={{ padding: '2rem 1rem' }}>
            <p className="state-block__title">Корзина пуста</p>
            <p className="state-block__lead">Удалённые задачи будут показаны здесь.</p>
          </div>
        ) : (
          <ul className="trash-list">
            {items.map((row) => (
              <li key={row.id} className="trash-row">
                <span className="trash-row__title">{row.title}</span>
                <div className="trash-row__actions">
                  <Button
                    type="button"
                    variant={ButtonVariants.FILLED}
                    color={ButtonColors.PRIMARY}
                    size={ButtonSizes.SMALL}
                    disabled={restorePending}
                    onClick={() => onRestore(row.id)}
                  >
                    Восстановить
                  </Button>
                  <Button
                    type="button"
                    variant={ButtonVariants.FILLED}
                    color={ButtonColors.DANGER}
                    size={ButtonSizes.SMALL}
                    disabled={purgePending}
                    onClick={() => onPurge(row.id)}
                  >
                    Навсегда
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        )}
        <div className="modal-form__footer">
          <div className="modal-actions">
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
