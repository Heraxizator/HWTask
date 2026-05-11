import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { cleanup, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { login } from '../../api/auth';
import { LoginPage } from './LoginPage';

vi.mock('../../api/auth', () => ({
  logout: vi.fn(() => Promise.resolve()),
  // /api/v1/auth/csrf warmup
  // (fetchJson is mocked only indirectly, so here we just keep the call sites happy)
  // login/register mocks return user only (cookie-session mode).
  login: vi.fn(() =>
    Promise.resolve({
      user: { id: '1', email: 'demo@hwtask.local', displayName: 'Demo' },
    }),
  ),
  register: vi.fn(() =>
    Promise.resolve({
      user: { id: '2', email: 'a@b.co', displayName: 'A' },
    }),
  ),
}));

vi.mock('../../api/http', () => ({
  ApiError: class extends Error {},
}));

function ui() {
  const client = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  const onLoggedIn = vi.fn();
  const view = render(
    <QueryClientProvider client={client}>
      <LoginPage onLoggedIn={onLoggedIn} />
    </QueryClientProvider>,
  );
  return { ...view, onLoggedIn };
}

beforeEach(() => {
  vi.stubGlobal(
    'matchMedia',
    vi.fn().mockImplementation((query: string) => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  );
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
  vi.unstubAllGlobals();
});

describe('LoginPage smoke', () => {
  it('shows login fields and submit', () => {
    ui();
    expect(screen.getByRole('tab', { name: /Вход/i })).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByRole('button', { name: /Войти/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Пароль')).toBeInTheDocument();
  });

  it('switches to register: shows name field and different submit label', async () => {
    const user = userEvent.setup();
    ui();
    await user.click(screen.getByRole('tab', { name: /Регистрация/i }));
    expect(screen.getByRole('tab', { name: /Регистрация/i })).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByPlaceholderText(/Как к вам обращаться/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Зарегистрироваться/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Не менее 6 символов/i)).toBeInTheDocument();
  });

  it('switching back to login hides display name field', async () => {
    const user = userEvent.setup();
    ui();
    await user.click(screen.getByRole('tab', { name: /Регистрация/i }));
    expect(screen.getByPlaceholderText(/Как к вам обращаться/i)).toBeInTheDocument();
    await user.click(screen.getByRole('tab', { name: /Вход/i }));
    expect(screen.queryByPlaceholderText(/Как к вам обращаться/i)).not.toBeInTheDocument();
    expect(screen.getByPlaceholderText('Пароль')).toBeInTheDocument();
  });

  it('submits login and calls onLoggedIn', async () => {
    const user = userEvent.setup();
    const { onLoggedIn } = ui();
    await user.click(screen.getByRole('button', { name: /Войти/i }));
    await vi.waitFor(() => {
      expect(onLoggedIn).toHaveBeenCalled();
    });
    expect(vi.mocked(login)).toHaveBeenCalledWith('demo@hwtask.local', 'demo');
  });
});
