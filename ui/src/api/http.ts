import type { ProblemDetailBody } from '../types/task';
import { refreshSession } from './auth';

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

function readCookie(name: string): string | undefined {
  if (typeof document === 'undefined') return undefined;
  const parts = document.cookie.split(';').map((p) => p.trim());
  for (const p of parts) {
    if (!p) continue;
    const idx = p.indexOf('=');
    if (idx < 0) continue;
    const k = decodeURIComponent(p.slice(0, idx));
    if (k !== name) continue;
    return decodeURIComponent(p.slice(idx + 1));
  }
  return undefined;
}

function authHeaders(init?: HeadersInit): Headers {
  const headers = new Headers(init);
  const csrf = readCookie('XSRF-TOKEN');
  if (csrf && !headers.has('X-XSRF-TOKEN')) {
    headers.set('X-XSRF-TOKEN', csrf);
  }
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }
  return headers;
}

async function retryOnceAfterRefresh<T>(fn: () => Promise<T>): Promise<T> {
  try {
    return await fn();
  } catch (e) {
    if (e instanceof ApiError && e.status === 401) {
      // avoid recursion for auth endpoints
      await refreshSession();
      return await fn();
    }
    throw e;
  }
}

export async function fetchJson<T>(
  input: RequestInfo,
  init?: RequestInit,
): Promise<T> {
  const url = typeof input === 'string' ? input : input.toString();
  const shouldRetry = !url.startsWith('/api/v1/auth/');
  const run = async () => {
    const res = await fetch(input, {
      ...init,
      credentials: 'include',
      headers: authHeaders(init?.headers),
    });
    return parseJsonResponse<T>(res);
  };
  return shouldRetry ? retryOnceAfterRefresh(run) : run();
}

/** DELETE / 204 без тела */
export async function fetchVoid(input: RequestInfo, init?: RequestInit): Promise<void> {
  const url = typeof input === 'string' ? input : input.toString();
  const shouldRetry = !url.startsWith('/api/v1/auth/');
  const run = async () => {
    const res = await fetch(input, {
      ...init,
      credentials: 'include',
      headers: authHeaders(init?.headers),
    });
    if (res.ok && res.status === 204) return;
    await parseJsonResponse<unknown>(res);
  };
  return shouldRetry ? retryOnceAfterRefresh(run) : run();
}

/** Авторизованный fetch бинарного ответа (скачивание вложений). */
export async function fetchBlob(input: RequestInfo, init?: RequestInit): Promise<Blob> {
  const url = typeof input === 'string' ? input : input.toString();
  const shouldRetry = !url.startsWith('/api/v1/auth/');
  const run = async () => {
    const res = await fetch(input, {
      ...init,
      credentials: 'include',
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
  };
  return shouldRetry ? retryOnceAfterRefresh(run) : run();
}

/** POST multipart/form-data (поле Content-Type задаёт браузер). */
export async function fetchJsonMultipart<T>(
  input: RequestInfo,
  formData: FormData,
): Promise<T> {
  const headers = authHeaders();
  const url = typeof input === 'string' ? input : input.toString();
  const shouldRetry = !url.startsWith('/api/v1/auth/');
  const run = async () => {
    const res = await fetch(input, { method: 'POST', credentials: 'include', headers, body: formData });
    return parseJsonResponse<T>(res);
  };
  return shouldRetry ? retryOnceAfterRefresh(run) : run();
}
