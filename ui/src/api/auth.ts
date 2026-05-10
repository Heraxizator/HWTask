import type { AuthResponse } from '../types/task';
import { fetchJson } from './http';

export async function login(email: string, password: string): Promise<AuthResponse> {
  return fetchJson<AuthResponse>('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
}

export async function register(
  email: string,
  password: string,
  displayName: string,
): Promise<AuthResponse> {
  return fetchJson<AuthResponse>('/api/v1/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, displayName }),
  });
}
