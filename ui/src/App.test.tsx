import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';

import App from './App';
import { ApiError } from './api/http';

vi.mock('./api/me', () => ({
  getMe: vi.fn(),
}));

vi.mock('./features/auth/LoginPage', () => ({
  LoginPage: () => <div>LOGIN_PAGE</div>,
}));

vi.mock('./features/tasks/TasksPage', () => ({
  TasksPage: () => <div>TASKS_PAGE</div>,
}));

import { getMe } from './api/me';

describe('App auth gate', () => {
  it('shows LoginPage when /me is unauthorized', async () => {
    vi.mocked(getMe).mockRejectedValueOnce(new ApiError('Unauthorized', 401));
    render(<App />);
    expect(await screen.findByText('LOGIN_PAGE')).toBeInTheDocument();
  });

  it('shows TasksPage when /me succeeds', async () => {
    vi.mocked(getMe).mockResolvedValueOnce({ id: '1', email: 'a@b.c', displayName: 'A' });
    render(<App />);
    expect(await screen.findByText('TASKS_PAGE')).toBeInTheDocument();
  });
});

