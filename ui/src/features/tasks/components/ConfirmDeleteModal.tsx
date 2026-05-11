import {
  Button,
  ButtonColors,
  ButtonSizes,
  ButtonVariants,
} from '../../../portal-ui';

export function ConfirmDeleteModal({
  title,
  description,
  errorMessage,
  loading,
  onCancel,
  onConfirm,
}: {
  title: string;
  description: string;
  errorMessage: string | null;
  loading: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}) {
  return (
    <div className="modal-backdrop" role="presentation" onClick={(ev) => {
      if (ev.target === ev.currentTarget) onCancel();
    }}>
      <div className="modal panel" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
        <h2 id="confirm-title">{title}</h2>
        <p className="muted" style={{ marginTop: 0, lineHeight: 1.5 }}>
          {description}
        </p>
        {errorMessage && (
          <div className="alert" role="alert" style={{ marginTop: '1rem' }}>
            {errorMessage}
          </div>
        )}
        <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
          <Button
            type="button"
            variant={ButtonVariants.GHOST}
            color={ButtonColors.NEUTRAL}
            size={ButtonSizes.MEDIUM}
            onClick={onCancel}
            disabled={loading}
          >
            Отмена
          </Button>
          <Button
            type="button"
            variant={ButtonVariants.FILLED}
            color={ButtonColors.DANGER}
            size={ButtonSizes.MEDIUM}
            loading={loading}
            onClick={onConfirm}
          >
            Удалить
          </Button>
        </div>
      </div>
    </div>
  );
}
