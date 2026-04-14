import { apiFetch } from './api';

export type { ApiError } from './api';

// ── Tipi ──────────────────────────────────────────────────────────────────────

export interface RegisterPayload {
  username:       string;
  email:          string;
  firstName:      string;
  lastName:       string;
  password:       string;
  requestedRoleIds: string[];
}

// ── API ───────────────────────────────────────────────────────────────────────

/**
 * POST /auth/register
 *
 * Crea un nuovo utente sul backend, che lo registra su Keycloak con stato pending.
 * requestedRoles per la self-registrazione è sempre ['user']:
 * l'admin approverà e assegnerà il ruolo definitivo.
 */
export async function register(payload: RegisterPayload): Promise<void> {
  return apiFetch('/auth/register', { method: 'POST', body: payload });
}
