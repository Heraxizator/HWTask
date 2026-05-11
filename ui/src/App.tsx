import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getMe } from './api/me';
import { logout } from './api/auth';
import { LoginPage } from './features/auth/LoginPage';
import { TasksPage } from './features/tasks/TasksPage';
import { ApiError } from './api/http';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
    },
  },
});

function AppGate() {
  const meQuery = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    retry: false,
    staleTime: 60_000,
  });

  const onLoggedIn = useCallback(() => {
    void meQuery.refetch();
  }, [meQuery]);

  const onLogout = useCallback(async () => {
    try {
      await logout();
    } finally {
      queryClient.clear();
      await meQuery.refetch();
    }
  }, [meQuery]);

  if (meQuery.isLoading) return null;
  if (meQuery.isError) {
    const err = meQuery.error;
    if (err instanceof ApiError && (err.status === 401 || err.status === 403)) {
      return <LoginPage onLoggedIn={onLoggedIn} />;
    }
    return (
      <div className="app-shell">
        <div className="state-block state-block--rich" role="alert" aria-label="Ошибка загрузки профиля">
          <div className="state-block__title">Сервис временно недоступен</div>
          <p className="state-block__lead muted">
            Не удалось проверить сессию. Попробуйте обновить страницу или повторите позже.
          </p>
        </div>
      </div>
    );
  }
  return <TasksPage onLogout={onLogout} />;
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppGate />
    </QueryClientProvider>
  );
}
