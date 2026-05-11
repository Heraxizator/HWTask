import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { Button } from './Button';
import { ButtonColors, ButtonSizes, ButtonVariants } from './enums';

afterEach(() => {
  cleanup();
});

describe('portal-ui Button', () => {
  it('uses submit type when passed', () => {
    render(
      <Button type="submit" variant={ButtonVariants.FILLED} color={ButtonColors.PRIMARY}>
        Send
      </Button>,
    );
    expect(screen.getByRole('button', { name: /Send/i })).toHaveAttribute('type', 'submit');
  });

  it('is disabled when disabled prop is true', async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(
      <Button disabled variant={ButtonVariants.FILLED} color={ButtonColors.PRIMARY} onClick={onClick}>
        Go
      </Button>,
    );
    const btn = screen.getByRole('button', { name: /Go/i });
    expect(btn).toBeDisabled();
    await user.click(btn);
    expect(onClick).not.toHaveBeenCalled();
  });

  it('is disabled and shows spinner when loading', () => {
    render(
      <Button loading variant={ButtonVariants.FILLED} color={ButtonColors.PRIMARY}>
        Save
      </Button>,
    );
    const btn = screen.getByRole('button');
    expect(btn).toBeDisabled();
    expect(btn.querySelector('.portal-btn__spin')).toBeTruthy();
  });

  it('calls onClick when enabled', async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(
      <Button variant={ButtonVariants.GHOST} color={ButtonColors.NEUTRAL} onClick={onClick}>
        Tap
      </Button>,
    );
    await user.click(screen.getByRole('button', { name: /Tap/i }));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('iconOnly + loading omits text children', () => {
    render(
      <Button
        iconOnly
        loading
        variant={ButtonVariants.FILLED}
        color={ButtonColors.DANGER}
        size={ButtonSizes.MEDIUM}
      >
        Hidden
      </Button>,
    );
    const btn = screen.getByRole('button');
    expect(btn.textContent).not.toContain('Hidden');
    expect(btn.querySelector('.portal-btn__spin')).toBeTruthy();
  });
});
