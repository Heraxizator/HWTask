import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { ConfirmDeleteModal } from './ConfirmDeleteModal';

afterEach(() => {
  cleanup();
});

describe('ConfirmDeleteModal', () => {
  it('calls onConfirm when confirming delete', async () => {
    const user = userEvent.setup();
    const onConfirm = vi.fn();
    render(
      <ConfirmDeleteModal
        title="Удалить?"
        description="Описание."
        errorMessage={null}
        loading={false}
        onCancel={vi.fn()}
        onConfirm={onConfirm}
      />,
    );
    await user.click(screen.getByRole('button', { name: /Удалить$/i }));
    expect(onConfirm).toHaveBeenCalledTimes(1);
  });

  it('calls onCancel when clicking Отмена', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();
    render(
      <ConfirmDeleteModal
        title="Удалить?"
        description="Описание."
        errorMessage={null}
        loading={false}
        onCancel={onCancel}
        onConfirm={vi.fn()}
      />,
    );
    await user.click(screen.getByRole('button', { name: /Отмена/i }));
    expect(onCancel).toHaveBeenCalledTimes(1);
  });
});
