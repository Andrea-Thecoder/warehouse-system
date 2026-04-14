import { writable } from 'svelte/store';

// ── Tipi ──────────────────────────────────────────────────────────────────────

export interface AuthUser {
  username:  string;
  email:     string;
  firstName: string;
  lastName:  string;
  roles:     string[];
}

export interface AuthState {
  isAuthenticated: boolean;
  isLoading:       boolean;
  user:            AuthUser | null;
}

// ── Store ─────────────────────────────────────────────────────────────────────

const initialState: AuthState = {
  isAuthenticated: false,
  isLoading:       true,   // true finché initKeycloak() non completa
  user:            null,
};

function createAuthStore() {
  const { subscribe, set, update } = writable<AuthState>(initialState);

  return {
    subscribe,

    set: (state: AuthState) => set(state),

    /** Resetta a stato non autenticato (post-logout). */
    clear: () => set({ isAuthenticated: false, isLoading: false, user: null }),

    /** Segna il caricamento iniziale come completato senza autenticazione. */
    setUnauthenticated: () => update(s => ({ ...s, isLoading: false })),
  };
}

export const authStore = createAuthStore();
