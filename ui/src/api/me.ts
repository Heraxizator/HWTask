import type { UserPublicResponse } from '../types/task';
import { fetchJson } from './http';

export function getMe(): Promise<UserPublicResponse> {
  return fetchJson<UserPublicResponse>('/api/v1/me');
}
