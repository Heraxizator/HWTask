import { useMutation } from '@tanstack/react-query';
import { AlertCircle, KanbanSquare } from 'lucide-react';
import { useEffect, useState } from 'react';
import { login, register } from '../../api/auth';
import { ApiError, clearStoredToken, setStoredToken } from '../../api/http';
import {
  Button,
  ButtonColors,
  ButtonSizes,
  ButtonVariants,
  InputField,
  InputSizes,
  InputTypes,
  TabSizes,
  Tabs,
} from '../../portal-ui';

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
  const [tabSize, setTabSize] = useState<(typeof TabSizes)[keyof typeof TabSizes]>(TabSizes.MEDIUM);

  useEffect(() => {
    const mq = window.matchMedia('(min-width: 768px)');
    const apply = () => setTabSize(mq.matches ? TabSizes.LARGE : TabSizes.MEDIUM);
    apply();
    mq.addEventListener('change', apply);
    return () => mq.removeEventListener('change', apply);
  }, []);

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

            <nav className="portal-tabs-nav" role="tablist" aria-label="Режим авторизации">
              <Tabs
                id="tab-login"
                label="Вход"
                size={tabSize}
                isActive={mode === 'login'}
                onClick={() => switchMode('login')}
              />
              <Tabs
                id="tab-register"
                label="Регистрация"
                size={tabSize}
                isActive={mode === 'register'}
                onClick={() => switchMode('register')}
              />
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
                <div className="auth-form__fields">
                  {mode === 'register' && (
                    <InputField
                      id="dn"
                      name="displayName"
                      placeholder="Как к вам обращаться"
                      autoComplete="name"
                      value={displayName}
                      onChange={(v) => setDisplayName(v)}
                      size={InputSizes.SMALL}
                      type={InputTypes.DEFAULT}
                    />
                  )}
                  <InputField
                    id="em"
                    name="email"
                    placeholder="Email"
                    autoComplete="email"
                    value={email}
                    onChange={(v) => setEmail(v)}
                    size={InputSizes.SMALL}
                    type={InputTypes.EMAIL}
                  />
                  <InputField
                    id="pw"
                    name="password"
                    placeholder={mode === 'login' ? 'Пароль' : 'Не менее 6 символов'}
                    autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                    value={password}
                    onChange={(v) => setPassword(v)}
                    size={InputSizes.SMALL}
                    type={InputTypes.PASSWORD}
                  />
                </div>

                <Button
                  type="submit"
                  fullWidth
                  size={ButtonSizes.LARGE}
                  color={ButtonColors.PRIMARY}
                  variant={ButtonVariants.FILLED}
                  loading={authMut.isPending}
                  className="auth-submit"
                >
                  {mode === 'login' ? 'Войти' : 'Зарегистрироваться'}
                </Button>
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
