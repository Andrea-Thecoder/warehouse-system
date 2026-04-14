# SvelteKit + Keycloak — Architettura SPA

Guida completa per replicare l'architettura di autenticazione e gestione utenti usata in **warehouse-ui**.

---

## Stack

| Layer | Tecnologia |
|---|---|
| Framework | SvelteKit 2 (Svelte 4) — SPA pura, SSR disabilitato |
| CSS | Tailwind CSS v4 via `@tailwindcss/vite` |
| Linguaggio | TypeScript strict |
| Bundler | Vite 5 |
| Auth | keycloak-js (OIDC Authorization Code + PKCE S256) |
| Backend | Quarkus REST (o Spring Boot) |
| IAM | Keycloak 26 |

---

## Regole fondamentali

- **Nessun file `.server.ts`** — SvelteKit è solo routing + UI client-side
- **Nessuna Keycloak Admin API dal FE** — tutto passa dal backend REST
- **Token in `sessionStorage`** — non localStorage, non cookie
- **SSR disabilitato globalmente** via `src/routes/+layout.ts`

```ts
// src/routes/+layout.ts
export const ssr = false;
```

---

## Struttura file

```
src/
├── app.css                        # @import tailwindcss + design tokens
├── lib/
│   ├── config.ts                  # variabili VITE_ centralizzate
│   ├── api/
│   │   ├── api.ts                 # apiFetch<T> — wrapper HTTP generico
│   │   ├── auth.ts                # register()
│   │   ├── lookup.ts              # fetchRoles()
│   │   ├── admin.ts               # pending users: list, approve, reject
│   │   └── users.ts               # all users: list, update roles, disable, delete
│   ├── auth/
│   │   └── keycloak.ts            # singleton keycloak-js
│   └── stores/
│       └── authStore.ts           # Svelte writable store
└── routes/
    ├── +layout.ts                 # ssr = false
    ├── +layout.svelte             # navbar + auth guard
    ├── +page.svelte               # homepage pubblica
    ├── register/+page.svelte
    ├── login/+page.svelte
    ├── pending/+page.svelte
    ├── dashboard/+page.svelte     # ruolo USER
    └── admin/
        ├── +page.svelte           # ruolo ADMIN — pending registrations
        └── users/+page.svelte     # ruolo ADMIN — gestione utenti
```

---

## config.ts

```ts
export const config = {
  backend:  { url: import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8081' },
  keycloak: {
    url:      import.meta.env.VITE_KEYCLOAK_URL      ?? 'http://localhost:8080',
    realm:    import.meta.env.VITE_KEYCLOAK_REALM    ?? 'my-realm',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'my-client',
  },
} as const;
```

**.env**
```
VITE_BACKEND_URL=http://localhost:8081
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=my-realm
VITE_KEYCLOAK_CLIENT_ID=my-client
```

---

## apiFetch — wrapper HTTP generico

```ts
// src/lib/api/api.ts
import { keycloak } from '$lib/auth/keycloak';

const BASE_URL = `${config.backend.url}/api/v1/my-app`;

export async function apiFetch<TResponse = void, TBody = unknown>(
  path: string,
  options: { method?: HttpMethod; body?: TBody; headers?: Record<string,string>; auth?: boolean } = {}
): Promise<TResponse> {
  const { method = 'GET', body, headers = {}, auth = false } = options;

  const reqHeaders: Record<string, string> = { 'Content-Type': 'application/json', ...headers };

  if (auth) {
    // Legge keycloak.token (aggiornato automaticamente dopo ogni refresh)
    // con fallback a sessionStorage
    const token = keycloak.token ?? sessionStorage.getItem('access_token');
    if (token) reqHeaders['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: reqHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) await throwApiError(res);
  if (res.status === 204) return undefined as TResponse;
  return res.json() as Promise<TResponse>;
}
```

---

## keycloak.ts — singleton e auth flow

```ts
// src/lib/auth/keycloak.ts
import Keycloak from 'keycloak-js';

export const keycloak = new Keycloak({
  url: config.keycloak.url,
  realm: config.keycloak.realm,
  clientId: config.keycloak.clientId,
});

const TOKEN_KEY         = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

// Promise condivisa — elimina race condition layout/pagina
let initPromise: Promise<boolean> | null = null;

export function initKeycloak(): Promise<boolean> {
  if (initPromise) return initPromise;

  const storedToken        = sessionStorage.getItem(TOKEN_KEY)         ?? undefined;
  const storedRefreshToken = sessionStorage.getItem(REFRESH_TOKEN_KEY) ?? undefined;

  if (storedRefreshToken) {
    // Refresh token presente → keycloak-js ripristina la sessione automaticamente.
    // Se l'access token è scaduto lo refresha con il refresh token.
    // Se anche il refresh token è scaduto → clearTokens() + return false.
    initPromise = keycloak
      .init({ pkceMethod: 'S256', checkLoginIframe: false, token: storedToken, refreshToken: storedRefreshToken })
      .then(authenticated => {
        if (authenticated) { persistTokens(); setupTokenRefresh(); syncStore(); }
        else clearTokens();
        return authenticated;
      })
      .catch(() => { clearTokens(); return false; });
  } else {
    // Nessun refresh token → init passivo (zero redirect automatici)
    initPromise = keycloak
      .init({ pkceMethod: 'S256', checkLoginIframe: false })
      .then(authenticated => {
        if (authenticated) { persistTokens(); setupTokenRefresh(); syncStore(); }
        return authenticated;
      });
  }

  return initPromise;
}

export function login(redirectUri?: string): void {
  keycloak.login({ redirectUri: redirectUri ?? window.location.href });
}

export function logout(): void {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  authStore.clear();
  keycloak.logout({ redirectUri: window.location.origin });
}

// Ruoli sempre UPPERCASE per consistenza con @RolesAllowed backend
export function getRoles(): string[] {
  return (keycloak.tokenParsed?.realm_access?.roles ?? []).map(r => r.toUpperCase());
}

export function getPostLoginRoute(): string {
  const roles = getRoles();
  if (roles.includes('ADMIN')) return '/admin';
  if (roles.includes('USER'))  return '/dashboard';
  return '/pending';
}

function setupTokenRefresh(): void {
  // Mid-session: refresh automatico quando il token sta per scadere
  keycloak.onTokenExpired = async () => {
    try {
      const refreshed = await keycloak.updateToken(30);
      if (refreshed) persistTokens();
    } catch {
      logout(); // refresh token scaduto
    }
  };
  keycloak.onAuthLogout = () => {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    authStore.clear();
  };
}
```

**Perché `checkLoginIframe: false`**: i browser moderni bloccano i cookie di terze parti usati dall'iframe di Keycloak.

**Perché `initPromise`**: layout e pagina chiamano entrambi `initKeycloak()`. Senza la Promise condivisa, il secondo a chiamarla riceverebbe `keycloak.authenticated = undefined` (init non ancora completato) e tornerebbe `false`.

---

## authStore.ts

```ts
import { writable } from 'svelte/store';

export interface AuthUser {
  username: string; email: string; firstName: string; lastName: string; roles: string[];
}
export interface AuthState {
  isAuthenticated: boolean; isLoading: boolean; user: AuthUser | null;
}

function createAuthStore() {
  const { subscribe, set, update } = writable<AuthState>({
    isAuthenticated: false, isLoading: true, user: null,
  });
  return {
    subscribe,
    set: (state: AuthState) => set(state),
    clear: () => set({ isAuthenticated: false, isLoading: false, user: null }),
    setUnauthenticated: () => update(s => ({ ...s, isLoading: false })),
  };
}
export const authStore = createAuthStore();
```

---

## +layout.svelte — auth guard + navbar dinamica

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { goto, afterNavigate } from '$app/navigation';
  import { initKeycloak, logout } from '$lib/auth/keycloak';
  import { authStore } from '$lib/stores/authStore';

  const PUBLIC_ROUTES = ['/', '/login', '/register', '/pending'];
  let currentPath = '/';

  afterNavigate(({ to }) => { currentPath = to?.url.pathname ?? window.location.pathname; });

  onMount(async () => {
    currentPath = window.location.pathname;
    const isPublic = PUBLIC_ROUTES.some(r => currentPath === r);

    // Init sempre (anche su route pubbliche) per risolvere isLoading
    // e mostrare i bottoni Accedi/Registrati
    const authenticated = await initKeycloak();

    if (!authenticated) {
      authStore.setUnauthenticated();
      if (!isPublic) goto('/login', { replaceState: true });
    }
  });
</script>
```

**Pattern chiave**: `initKeycloak()` viene chiamato su TUTTE le route (anche pubbliche) perché se non viene chiamato, `authStore.isLoading` resta `true` e i bottoni Accedi/Registrati non appaiono mai.

---

## login/+page.svelte — post-login redirect

```svelte
onMount(async () => {
  try {
    const authenticated = await initKeycloak();
    if (authenticated) {
      goto(getPostLoginRoute(), { replaceState: true }); // → /admin o /dashboard
      return;
    }
    authStore.setUnauthenticated();
  } catch (e) {
    authStore.setUnauthenticated();
  } finally {
    checking = false;
  }
});

// Bottone login — redirect URI = /login per gestire il codice OAuth qui
<button on:click={() => login(`${window.location.origin}/login`)}>
  Accedi con Keycloak
</button>
```

---

## Protezione route (guard pattern)

Ogni pagina protetta:

```svelte
onMount(async () => {
  await initKeycloak();

  if (!getRoles().includes('ADMIN')) {
    goto('/', { replaceState: true });
    return;
  }

  // carica dati...
});
```

---

## Paginazione — pattern riutilizzabile

```ts
// Interfaccia generica per tutte le risposte paginate
export interface PagedResult<T> {
  list:       T[];
  totalRows:  number;
  totalPages: number;
  pageSize:   number;
  page:       number;  // 1-based (backend si aspetta page >= 1)
}

// Query params con sort e descending
export interface SearchParams {
  page?:       number;  // min 1
  size?:       number;
  sort?:       string;
  descending?: boolean;
}

function buildQuery(params: SearchParams): string {
  const q = new URLSearchParams();
  q.set('page', String(params.page ?? 1));
  q.set('size', String(params.size ?? 20));
  if (params.sort       != null) q.set('sort',       params.sort);
  if (params.descending != null) q.set('descending', String(params.descending));
  return q.toString();
}
```

---

## Keycloak — configurazione client

| Campo | Valore |
|---|---|
| Client type | OpenID Connect |
| Client authentication | OFF (public client) |
| Standard flow | ON |
| Direct access grants | OFF |
| Valid redirect URIs | `http://localhost:5173/*` |
| Web origins | `http://localhost:5173` |
| Realm roles | `ADMIN`, `USER`, `PENDING` (UPPERCASE) |

**Importante**: i ruoli Keycloak devono essere UPPERCASE per matchare `@RolesAllowed` di Quarkus e i check lato FE.

---

## Quarkus — configurazione OIDC

```properties
quarkus.oidc.auth-server-url=http://localhost:8080/realms/my-realm
quarkus.oidc.client-id=my-client
quarkus.oidc.application-type=service
quarkus.oidc.roles.role-claim-path=realm_access/roles
```

---

## Pattern API admin con DTO Java

```ts
// FE replica esattamente i campi del DTO Java (no campo in più)

// SimpleKeycloakUserDTO
interface UserDTO {
  id:        string;
  username:  string;
  firstName: string;
  lastName:  string;
  enabled:   boolean;
  roles:     SimpleRoleType[];   // { id: string; label: string }
}

// ApprovedInDTO (body approve)
approveRegistration(userId, roleIds) →
  POST /admin-managment/{id}/approve
  body: { approvedRoles: string[] }   // ← nome campo dal DTO Java
```

---

## Gotcha & lezioni imparate

| Problema | Soluzione |
|---|---|
| Bottoni navbar non appaiono | `initKeycloak()` va chiamato anche sulle route pubbliche |
| Race condition layout/pagina | Usare `initPromise` condivisa invece di flag booleano |
| `checkLoginIframe: false` | Browser bloccano i cookie 3p usati dall'iframe Keycloak |
| Ruoli case mismatch | Normalizzare sempre a UPPERCASE in `getRoles()` |
| F5 perde la sessione | Passare `token` e `refreshToken` da sessionStorage a `keycloak.init()` |
| 403 Forbidden | Verificare che i ruoli Keycloak matchino `@RolesAllowed` (case sensitive) |
| `apiFetch` manda token scaduto | Leggere `keycloak.token` (live) invece di sessionStorage |
| `page` 0-based vs 1-based | Quarkus `@Min(1)` → il FE parte sempre da `page=1` |
| Body array vs oggetto | `approveRegistration` invia `{ approvedRoles: [] }` non `[]` direttamente |
| `$app/stores` deprecato | Usare `afterNavigate` per tracciare il path corrente |
