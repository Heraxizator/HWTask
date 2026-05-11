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
        <p style={{ color: 'var(--color-ice-muted)', marginTop: 0 }}>{description}</p>
        {errorMessage && (
          <div className="alert" role="alert" style={{ marginTop: '1rem' }}>
            {errorMessage}
          </div>
        )}
        <div className="modal-actions" style={{ marginTop: '1.5rem' }}>
          <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={loading}>
            Отмена
          </button>
          <button type="button" className="btn btn-danger" onClick={onConfirm} disabled={loading}>
            {loading ? 'Удаление…' : 'Удалить'}
          </button>
        </div>
      </div>
    </div>
  );
}
