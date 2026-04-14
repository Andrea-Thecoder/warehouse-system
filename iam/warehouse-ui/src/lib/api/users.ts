import { apiFetch } from './api';
import type { SimpleRoleType, PagedResult } from './admin';

// ── Tipi ──────────────────────────────────────────────────────────────────────

export interface UserDTO {
  id:        string;
  username:  string;
  firstName: string;
  lastName:  string;
  enabled:   boolean;
  roles:     SimpleRoleType[];
}

// ── API ───────────────────────────────────────────────────────────────────────

export interface SearchParams {
  page?:       number;
  size?:       number;
  sort?:       string;
  descending?: boolean;
}

function buildQuery(params: SearchParams): string {
  const q = new URLSearchParams();
  q.set('page', String(params.page  ?? 1));
  q.set('size', String(params.size  ?? 20));
  if (params.sort      != null) q.set('sort',       params.sort);
  if (params.descending != null) q.set('descending', String(params.descending));
  return q.toString();
}

/**
 * GET /admin-managment/users
 * Lista paginata di tutti gli utenti (attivi e non).
 * Supporta: page (min 1), size, sort, descending.
 */
export async function getUsers(params: SearchParams = {}): Promise<PagedResult<UserDTO>> {
  return apiFetch<PagedResult<UserDTO>>(
    `/admin-managment/users?${buildQuery(params)}`,
    { auth: true },
  );
}

/**
 * PUT /admin-managment/users/{userId}/roles
 * Body: { approvedRoles: string[] } — stesso DTO di approve.
 */
export async function updateUserRoles(userId: string, roleIds: string[]): Promise<void> {
  return apiFetch(`/admin-managment/users/${userId}/roles`, {
    method: 'PUT',
    body:   { approvedRoles: roleIds },
    auth:   true,
  });
}

/**
 * PATCH /admin-managment/users/{userId}/manager-enable?enable=<boolean>
 * Abilita (true) o disabilita (false) l'utente su Keycloak.
 */
export async function setUserEnabled(userId: string, enabled: boolean): Promise<void> {
  return apiFetch(`/admin-managment/users/${userId}/manager-enable?enable=${enabled}`, {
    method: 'PATCH',
    auth:   true,
  });
}

/**
 * DELETE /admin-managment/users/{userId}
 * Elimina definitivamente l'utente da Keycloak.
 */
export async function deleteUser(userId: string): Promise<void> {
  return apiFetch(`/admin-managment/users/${userId}`, {
    method: 'DELETE',
    auth:   true,
  });
}
