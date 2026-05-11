import type { ProblemDetailBody } from '../types/task';

export const TOKEN_STORAGE_KEY = 'hwtask_token';

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setStoredToken(token: string): void {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearStoredToken(): void {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
}

export class ApiError extends Error {
  readonly status: number;
  readonly problem?: ProblemDetailBody;

  constructor(message: string, status: number, problem?: ProblemDetailBody) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
  }
}

function readProblemMessage(body: unknown): string | undefined {
  if (!body || typeof body !== 'object') return undefined;
  const p = body as ProblemDetailBody;
  if (typeof p.detail === 'string' && p.detail.trim()) return p.detail;
  if (typeof p.title === 'string' && p.title.trim()) return p.title;
  return undefined;
}

export async function parseJsonResponse<T>(res: Response): Promise<T> {
  const text = await res.text();
  const body = text ? (JSON.parse(text) as unknown) : undefined;

  if (!res.ok) {
    const fromBody =
      body !== undefined && body !== null
        ? readProblemMessage(body)
        : undefined;
    const msg: string =
      fromBody?.trim() ||
      res.statusText?.trim() ||
      `HTTP ${res.status}`;
    const problem =
      body && typeof body === 'object' && 'status' in (body as object)
        ? (body as ProblemDetailBody)
        : undefined;
    throw new ApiError(msg, res.status, problem);
  }

  return body as T;
}

function authHeaders(init?: HeadersInit): Headers {
  const headers = new Headers(init);
  const token = getStoredToken();
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }
  return headers;
}

export async function fetchJson<T>(
  input: RequestInfo,
  init?: RequestInit,
): Promise<T> {
  const res = await fetch(input, {
    ...init,
    headers: authHeaders(init?.headers),
  });
  return parseJsonResponse<T>(res);
}

/** DELETE / 204 без тела */
export async function fetchVoid(input: RequestInfo, init?: RequestInit): Promise<void> {
  const res = await fetch(input, {
    ...init,
    headers: authHeaders(init?.headers),
  });
  if (res.ok && res.status === 204) return;
  await parseJsonResponse<unknown>(res);
}

/** Авторизованный fetch бинарного ответа (скачивание вложений). */
export async function fetchBlob(input: RequestInfo, init?: RequestInit): Promise<Blob> {
  const res = await fetch(input, {
    ...init,
    headers: authHeaders(init?.headers),
  });
  if (!res.ok) {
    const text = await res.text();
    let body: unknown;
    if (text) {
      try {
        body = JSON.parse(text) as unknown;
      } catch {
        body = undefined;
      }
    }
    const msg =
      (body !== undefined &&
        typeof body === 'object' &&
        readProblemMessage(body)) ||
      text?.trim() ||
      res.statusText?.trim() ||
      `HTTP ${res.status}`;
    const problem =
      body && typeof body === 'object' && 'status' in (body as object)
        ? (body as ProblemDetailBody)
        : undefined;
    throw new ApiError(msg, res.status, problem);
  }
  return res.blob();
}

/** POST multipart/form-data (поле Content-Type задаёт браузер). */
export async function fetchJsonMultipart<T>(
  input: RequestInfo,
  formData: FormData,
): Promise<T> {
  const headers = authHeaders();
  const res = await fetch(input, { method: 'POST', headers, body: formData });
  return parseJsonResponse<T>(res);
}
