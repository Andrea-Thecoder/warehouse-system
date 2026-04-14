import Keycloak from 'keycloak-js';
import { config } from '$lib/config';
import { authStore } from '$lib/stores/authStore';

// ── Singleton ─────────────────────────────────────────────────────────────────

export const keycloak = new Keycloak({
  url:      config.keycloak.url,
  realm:    config.keycloak.realm,
  clientId: config.keycloak.clientId,
});

// Promise condivisa: chiunque chiami initKeycloak() aspetta lo stesso risultato,
// eliminando la race condition tra layout e pagina.
let initPromise: Promise<boolean> | null = null;

// ── SessionStorage keys ───────────────────────────────────────────────────────

const TOKEN_KEY         = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

// ── Init ──────────────────────────────────────────────────────────────────────

/**
 * Inizializza keycloak-js con PKCE S256 e check-sso silenzioso.
 * - Se l'utente ha già una sessione Keycloak attiva → autentica automaticamente
 * - Se non autenticato → restituisce false senza redirect (il login è esplicito)
 *
 * Da chiamare una sola volta, nel +layout.svelte (onMount).
 */
export function initKeycloak(): Promise<boolean> {
  if (initPromise) return initPromise;

  const storedToken        = sessionStorage.getItem(TOKEN_KEY)         ?? undefined;
  const storedRefreshToken = sessionStorage.getItem(REFRESH_TOKEN_KEY) ?? undefined;

  if (storedRefreshToken) {
    // Refresh token presente → keycloak-js prova a ripristinare la sessione.
    // Se il token è scaduto lo refresha automaticamente usando il refresh token.
    // Se anche il refresh token è scaduto → authenticated = false → clear + false.
    initPromise = keycloak
      .init({
        pkceMethod:       'S256',
        checkLoginIframe: false,
        token:            storedToken,
        refreshToken:     storedRefreshToken,
      })
      .then(authenticated => {
        if (authenticated) {
          persistTokens();
          setupTokenRefresh();
          syncStore();
        } else {
          // Refresh token scaduto o revocato
          clearTokens();
        }
        return authenticated;
      })
      .catch(() => {
        clearTokens();
        return false;
      });
  } else {
    // Nessun refresh token → init passivo standard (zero redirect automatici)
    initPromise = keycloak
      .init({ pkceMethod: 'S256', checkLoginIframe: false })
      .then(authenticated => {
        if (authenticated) {
          persistTokens();
          setupTokenRefresh();
          syncStore();
        }
        return authenticated;
      });
  }

  return initPromise;
}

// ── Login / Logout ────────────────────────────────────────────────────────────

/**
 * Redirige l'utente alla pagina di login Keycloak.
 * Dopo l'autenticazione, Keycloak torna su redirectUri (default: pagina corrente).
 */
export function login(redirectUri?: string): void {
  keycloak.login({ redirectUri: redirectUri ?? window.location.href });
}

export function logout(): void {
  clearTokens();
  authStore.clear();
  keycloak.logout({ redirectUri: window.location.origin });
}

// ── Ruoli & redirect ──────────────────────────────────────────────────────────

/** Ruoli realm dell'utente loggato, sempre in UPPERCASE per consistenza. */
export function getRoles(): string[] {
  return (keycloak.tokenParsed?.realm_access?.roles ?? []).map(r => r.toUpperCase());
}

/** Determina la route di destinazione post-login in base al ruolo. */
export function getPostLoginRoute(): string {
  const roles = getRoles();
  if (roles.includes('ADMIN')) return '/admin';
  if (roles.includes('USER'))  return '/dashboard';
  return '/pending'; // account creato ma non ancora approvato
}

// ── Token helpers ─────────────────────────────────────────────────────────────

function persistTokens(): void {
  if (keycloak.token)        sessionStorage.setItem(TOKEN_KEY,         keycloak.token);
  if (keycloak.refreshToken) sessionStorage.setItem(REFRESH_TOKEN_KEY, keycloak.refreshToken);
}

function clearTokens(): void {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(REFRESH_TOKEN_KEY);
}

function setupTokenRefresh(): void {
  keycloak.onTokenExpired = async () => {
    try {
      const refreshed = await keycloak.updateToken(30);
      if (refreshed) persistTokens();
    } catch {
      // Refresh fallito: sessione scaduta, forza logout
      logout();
    }
  };

  keycloak.onAuthLogout = () => {
    clearTokens();
    authStore.clear();
  };
}

// ── Sync store ────────────────────────────────────────────────────────────────

function syncStore(): void {
  const p = keycloak.tokenParsed;
  if (!p) return;

  authStore.set({
    isAuthenticated: true,
    isLoading:       false,
    user: {
      username:  p['preferred_username'] ?? '',
      email:     p['email']              ?? '',
      firstName: p['given_name']         ?? '',
      lastName:  p['family_name']        ?? '',
      roles:     getRoles(), // già normalizzati in UPPERCASE da getRoles()
    },
  });
}
