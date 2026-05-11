import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { TabSizes } from './enums';
import { Tabs } from './Tabs';

afterEach(() => {
  cleanup();
});

describe('portal-ui Tabs', () => {
  it('sets aria-selected from isActive', () => {
    const { rerender } = render(<Tabs label="One" size={TabSizes.MEDIUM} isActive onClick={vi.fn()} id="t1" />);
    expect(screen.getByRole('tab', { name: 'One' })).toHaveAttribute('aria-selected', 'true');
    rerender(<Tabs label="One" size={TabSizes.MEDIUM} isActive={false} onClick={vi.fn()} id="t1" />);
    expect(screen.getByRole('tab', { name: 'One' })).toHaveAttribute('aria-selected', 'false');
  });

  it('adds active class when isActive', () => {
    const { container } = render(<Tabs label="A" size={TabSizes.LARGE} isActive onClick={vi.fn()} />);
    expect(container.querySelector('.portal-tab--active')).toBeTruthy();
  });

  it('calls onClick when enabled', async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(<Tabs label="Tap" size={TabSizes.MEDIUM} isActive={false} onClick={onClick} />);
    await user.click(screen.getByRole('tab', { name: 'Tap' }));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('does not fire onClick when disabled', async () => {
    const user = userEvent.setup();
    const onClick = vi.fn();
    render(<Tabs label="Off" size={TabSizes.MEDIUM} isActive={false} disabled onClick={onClick} />);
    const tab = screen.getByRole('tab', { name: 'Off' });
    expect(tab).toBeDisabled();
    await user.click(tab);
    expect(onClick).not.toHaveBeenCalled();
  });
});
