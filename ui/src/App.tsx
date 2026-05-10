import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useCallback, useState } from 'react';
import { getStoredToken, clearStoredToken } from './api/http';
import { LoginPage } from './features/auth/LoginPage';
import { TasksPage } from './features/tasks/TasksPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
    },
  },
});

export default function App() {
  const [tokenPresent, setTokenPresent] = useState(() => !!getStoredToken());

  const onLoggedIn = useCallback(() => {
    setTokenPresent(true);
  }, []);

  const onLogout = useCallback(() => {
    clearStoredToken();
    setTokenPresent(false);
    queryClient.clear();
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      {!tokenPresent ? (
        <LoginPage onLoggedIn={onLoggedIn} />
      ) : (
        <TasksPage onLogout={onLogout} />
      )}
    </QueryClientProvider>
  );
}
