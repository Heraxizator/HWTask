import { useMutation } from '@tanstack/react-query';
import { AlertCircle, KanbanSquare, Loader2 } from 'lucide-react';
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

  function switchMode(next: 'login' | 'register') {
    setMode(next);
    setError(null);
  }

  return (
    <main className="auth-page">
      <div className="auth-shell">
        <aside className="auth-banner" aria-label="О сервисе HWTask">
          <div className="auth-banner__gradient" aria-hidden />
          <div className="auth-banner__glow auth-banner__glow--1" aria-hidden />
          <div className="auth-banner__glow auth-banner__glow--2" aria-hidden />
          <div className="auth-banner__content">
            <div className="auth-banner__logo-row">
              <span className="auth-banner__logo-mark" aria-hidden>
                <KanbanSquare size={22} strokeWidth={2} />
              </span>
              <span className="auth-banner__logo-text">HWTask</span>
            </div>
            <h2 className="auth-banner__title">Задачи, проекты и совместная работа</h2>
            <p className="auth-banner__text">
              Единое место для оргструктуры, ролей, комментариев и отчётов — в привычном
              портальном интерфейсе.
            </p>
            <div className="auth-banner__dots" aria-hidden>
              <span className="auth-banner__dot auth-banner__dot--active" />
              <span className="auth-banner__dot" />
              <span className="auth-banner__dot" />
            </div>
          </div>
        </aside>

        <section className="auth-form-column" aria-label="Форма входа и регистрации">
          <div className="auth-card">
            <header className="auth-card__header">
              <div className="auth-card__brand">
                <span className="auth-card__brand-mark" aria-hidden>
                  <KanbanSquare size={20} strokeWidth={2} />
                </span>
                <h1 className="auth-card__title">HWTask</h1>
              </div>
              <p className="auth-card__subtitle">Вход в трекер задач организации</p>
            </header>

            <nav className="auth-tabs" role="tablist" aria-label="Режим авторизации">
              <button
                type="button"
                role="tab"
                id="tab-login"
                aria-selected={mode === 'login'}
                aria-controls="auth-panel"
                className="auth-tab"
                onClick={() => switchMode('login')}
              >
                Вход
              </button>
              <button
                type="button"
                role="tab"
                id="tab-register"
                aria-selected={mode === 'register'}
                aria-controls="auth-panel"
                className="auth-tab"
                onClick={() => switchMode('register')}
              >
                Регистрация
              </button>
            </nav>

            <div id="auth-panel" role="tabpanel" aria-labelledby={mode === 'login' ? 'tab-login' : 'tab-register'}>
              {error && (
                <div className="auth-alert" role="alert">
                  <AlertCircle size={18} strokeWidth={2} aria-hidden />
                  <span>{error}</span>
                </div>
              )}

              <form
                className="auth-form"
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
                      placeholder="Как к вам обращаться"
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
                    placeholder="name@company.ru"
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
                    placeholder={mode === 'login' ? 'Введите пароль' : 'Не менее 6 символов'}
                    required
                    minLength={mode === 'register' ? 6 : undefined}
                  />
                </div>
                <button type="submit" className="btn btn-primary auth-submit" disabled={authMut.isPending}>
                  {authMut.isPending ? (
                    <>
                      <Loader2 size={18} strokeWidth={2} className="spin-inline" aria-hidden />
                      Подождите…
                    </>
                  ) : mode === 'login' ? (
                    'Войти'
                  ) : (
                    'Зарегистрироваться'
                  )}
                </button>
              </form>
            </div>

            <p className="auth-hint">
              Локально после первого запуска API доступен демо-доступ:{' '}
              <strong>demo@hwtask.local</strong> / <strong>demo</strong>.
            </p>
          </div>
        </section>
      </div>
    </main>
  );
}
