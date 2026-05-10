import { useMutation } from '@tanstack/react-query';
import { useState } from 'react';
import { login, register } from '../../api/auth';
import { ApiError, clearStoredToken, setStoredToken } from '../../api/http';

export function LoginPage({
  onLoggedIn,
}: {
  onLoggedIn: () => void;
}) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('demo@hwtask.local');
  const [password, setPassword] = useState('demo');
  const [displayName, setDisplayName] = useState('');
  const [error, setError] = useState<string | null>(null);

  const authMut = useMutation({
    mutationFn: async () => {
      clearStoredToken();
      if (mode === 'login') {
        return login(email.trim(), password);
      }
      return register(email.trim(), password, displayName.trim() || email.trim());
    },
    onSuccess: (data) => {
      setStoredToken(data.accessToken);
      setError(null);
      onLoggedIn();
    },
    onError: (e: Error) => {
      setError(e instanceof ApiError ? e.message : e.message);
    },
  });

  return (
    <div className="app-shell" style={{ maxWidth: 420, margin: '4rem auto' }}>
      <div className="panel">
        <h1 className="app-title" style={{ marginTop: 0 }}>
          HWTask
        </h1>
        <p className="app-sub">Вход в трекер задач</p>

        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
          <button
            type="button"
            className={`btn ${mode === 'login' ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setMode('login')}
          >
            Вход
          </button>
          <button
            type="button"
            className={`btn ${mode === 'register' ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setMode('register')}
          >
            Регистрация
          </button>
        </div>

        {error && (
          <div className="alert" role="alert">
            {error}
          </div>
        )}

        <form
          onSubmit={(e) => {
            e.preventDefault();
            authMut.mutate();
          }}
        >
          {mode === 'register' && (
            <div className="field">
              <label htmlFor="dn">Имя</label>
              <input
                id="dn"
                value={displayName}
                onChange={(ev) => setDisplayName(ev.target.value)}
                autoComplete="name"
              />
            </div>
          )}
          <div className="field">
            <label htmlFor="em">Email</label>
            <input
              id="em"
              type="email"
              value={email}
              onChange={(ev) => setEmail(ev.target.value)}
              autoComplete="email"
              required
            />
          </div>
          <div className="field">
            <label htmlFor="pw">Пароль</label>
            <input
              id="pw"
              type="password"
              value={password}
              onChange={(ev) => setPassword(ev.target.value)}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              required
              minLength={mode === 'register' ? 6 : undefined}
            />
          </div>
          <button type="submit" className="btn btn-primary" disabled={authMut.isPending}>
            {authMut.isPending ? '…' : mode === 'login' ? 'Войти' : 'Зарегистрироваться'}
          </button>
        </form>
        <p className="muted" style={{ marginTop: '1rem', fontSize: '0.85rem' }}>
          Локально (dev): demo@hwtask.local / demo после первого запуска API.
        </p>
      </div>
    </div>
  );
}
