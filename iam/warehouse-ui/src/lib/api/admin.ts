import { apiFetch } from './api';

// ── Tipi ──────────────────────────────────────────────────────────────────────

export interface SimpleRoleType {
  id:    string;
  label: string;
}

export interface PendingUser {
  id:    string;   // UUID Keycloak
  roles: SimpleRoleType[];
}

export interface PagedResult<T> {
  list:       T[];
  totalRows:  number;
  totalPages: number;
  pageSize:   number;
  page:       number;
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
  if (params.sort       != null) q.set('sort',       params.sort);
  if (params.descending != null) q.set('descending', String(params.descending));
  return q.toString();
}

/**
 * GET /admin-managment/user-pending
 * Lista paginata degli utenti in attesa di approvazione.
 * Supporta: page (min 1), size, sort, descending.
 */
export async function getPendingRegistrations(
  params: SearchParams = { size: 100 },
): Promise<PagedResult<PendingUser>> {
  return apiFetch<PagedResult<PendingUser>>(
    `/admin-managment/user-pending?${buildQuery(params)}`,
    { auth: true },
  );
}

/**
 * POST /admin-managment/{id}/approve
 * Body: ApprovedInDTO { approvedRoles: string[] }
 * Usato sia per "approva tutto" sia per "approva con selezione ruoli".
 */
export async function approveRegistration(userId: string, roleIds: string[]): Promise<void> {
  return apiFetch(`/admin-managment/${userId}/approve`, {
    method: 'POST',
    body:   { approvedRoles: roleIds },
    auth:   true,
  });
}

/** POST /admin-managment/{id}/reject — rifiuta la registrazione */
export async function rejectRegistration(userId: string): Promise<void> {
  return apiFetch(`/admin-managment/${userId}/reject`, {
    method: 'POST',
    auth:   true,
  });
}
