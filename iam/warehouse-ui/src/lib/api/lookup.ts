import { apiFetch } from './api';

export interface RoleType {
  id:          string;
  label:       string;
  description: string;
}

/**
 * GET /lookup/roles
 * Restituisce la lista dei ruoli disponibili per la registrazione.
 */
export async function fetchRoles(): Promise<RoleType[]> {
  return apiFetch<RoleType[]>('/lookup/roles');
}
