import { config } from '$lib/config';
import { keycloak } from '$lib/auth/keycloak';

// ── Tipi ──────────────────────────────────────────────────────────────────────

export interface ApiError {
  status:  number;
  message: string;
}

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

interface RequestOptions<TBody> {
  method?:  HttpMethod;
  body?:    TBody;
  headers?: Record<string, string>;
  /** Se true, allega il JWT da sessionStorage nell'header Authorization. */
  auth?:    boolean;
}

// ── Base URL ──────────────────────────────────────────────────────────────────

const BASE_URL = `${config.backend.url}/api/v1/warehouse-administrator`;

// ── Core fetch ────────────────────────────────────────────────────────────────

/**
 * Wrapper generico attorno a fetch.
 * - Prepone BASE_URL al path
 * - Serializza il body in JSON
 * - Allega il token JWT se `auth: true`
 * - Lancia un ApiError tipizzato su risposta non-2xx
 *
 * @example
 * const user = await apiFetch<UserDTO>('/users/me', { auth: true });
 * await apiFetch('/auth/register', { method: 'POST', body: payload });
 */
export async function apiFetch<TResponse = void, TBody = unknown>(
  path: string,
  options: RequestOptions<TBody> = {}
): Promise<TResponse> {
  const { method = 'GET', body, headers = {}, auth = false } = options;

  const reqHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...headers,
  };

  if (auth) {
    // Legge keycloak.token (sempre aggiornato da keycloak-js dopo ogni refresh)
    // con fallback a sessionStorage nel caso l'istanza non sia ancora inizializzata.
    const token = keycloak.token ?? sessionStorage.getItem('access_token');
    if (token) {
      reqHeaders['Authorization'] = `Bearer ${token}`;
    }
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: reqHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    await throwApiError(res);
  }

  // 204 No Content — nessun body da parsare
  if (res.status === 204) {
    return undefined as TResponse;
  }

  return res.json() as Promise<TResponse>;
}

// ── Helper errore ─────────────────────────────────────────────────────────────

async function throwApiError(res: Response): Promise<never> {
  let message = `Errore ${res.status}`;

  try {
    const body = await res.json();

    // Spring Boot MethodArgumentNotValidException → { errors: [{field, defaultMessage}] }
    if (Array.isArray(body?.errors) && body.errors.length > 0) {
      const first = body.errors[0];
      message = typeof first === 'string'
        ? first
        : `${first.field ? first.field + ': ' : ''}${first.defaultMessage ?? first.message ?? message}`;

    // Spring Boot standard → { message } oppure { error }
    } else {
      message = body?.message ?? body?.error ?? message;
    }

    // Log completo in console per debug
    console.error('[API error]', res.status, body);
  } catch {
    // body non è JSON
  }

  throw { status: res.status, message } satisfies ApiError;
}
