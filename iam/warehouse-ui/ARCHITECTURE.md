# SvelteKit SPA — Architectural Reference

Guida generica e riutilizzabile per costruire una SPA con SvelteKit, autenticazione Keycloak e backend REST.  
Tutte le regole, pattern e gotcha derivano dall'esperienza diretta sul progetto e possono essere copiate su qualsiasi progetto FE con lo stesso stack.

---

## Stack

| Layer | Tecnologia |
|---|---|
| Framework | SvelteKit 2 (Svelte 4) — SPA pura, SSR disabilitato |
| CSS | Tailwind CSS v4 via `@tailwindcss/vite` |
| Linguaggio | TypeScript (`strict: true`) |
| Bundler | Vite 5 |
| Auth | keycloak-js — OIDC Authorization Code + PKCE S256 |
| Backend | REST API (es. Quarkus, Spring Boot) |
| IAM | Keycloak 26+ |

---

## Regole fondamentali (non derogabili)

### SvelteKit è solo client-side

- **Vietato** creare file `*.server.ts` / `*.server.js` o qualsiasi file `.server.`
- **Vietato** usare `load` server-side, `actions` SvelteKit o `import { ... } from '$app/server'`
- SSR va disabilitato **globalmente** in `src/routes/+layout.ts`

```ts
// src/routes/+layout.ts
export const ssr = false;
```

### Tutto il privileged work passa dal backend

- Il FE non chiama mai la Keycloak Admin API direttamente
- Operazioni privilegiate: `FE → POST backend → backend chiama Keycloak Admin API`
- Nessun secret o credenziale admin nel FE; solo variabili `VITE_` (pubbliche per definizione)

### Token storage

- I token JWT (access + refresh) vanno in `sessionStorage`, non in `localStorage` né in cookie
- Chiavi: `'access_token'` e `'refresh_token'`

### Variabili d'ambiente

- Prefisso obbligatorio: `VITE_` (es. `VITE_BACKEND_URL`, `VITE_KEYCLOAK_URL`)
- Nessun `.env` committato nel repository
- In produzione usare il pattern di runtime injection (vedi sezione dedicata)

---

## Struttura `src/`

```
src/
├── app.html                        # template HTML root
├── app.css                         # @import tailwindcss + design tokens
├── app.d.ts                        # tipi globali App namespace
├── lib/
│   ├── config.ts                   # costanti centralizzate (VITE_ + runtime injection)
│   ├── api/
│   │   ├── api.ts                  # apiFetch<T> — wrapper HTTP generico
│   │   ├── auth.ts                 # registrazione utente → chiama backend
│   │   ├── lookup.ts               # dati statici (es. fetchRoles)
│   │   └── *.ts                    # un file per dominio (users, orders, ...)
│   ├── auth/
│   │   └── keycloak.ts             # singleton keycloak-js
│   └── stores/
│       └── authStore.ts            # Svelte writable store per stato auth
└── routes/
    ├── +layout.ts                  # ssr = false (globale)
    ├── +layout.svelte              # navbar + auth guard globale
    ├── +page.svelte                # homepage pubblica
    ├── login/+page.svelte          # trigger keycloak redirect + post-login routing
    ├── register/+page.svelte       # form registrazione → chiama backend
    ├── pending/+page.svelte        # account in attesa di approvazione
    ├── dashboard/+page.svelte      # protetta: ruolo USER
    └── admin/
        └── +page.svelte            # protetta: ruolo ADMIN
```

---

## config.ts — configurazione centralizzata

Strategia a tre livelli (priorità decrescente):
1. **Runtime**: `window.__APP_CONFIG__` — iniettato da un file `/config.js` generato a container startup. Permette di cambiare URL senza rebuilding dell'immagine Docker.
2. **Build-time**: `import.meta.env.VITE_*` — da file `.env` (sviluppo locale).
3. **Fallback hardcoded**: valori di default per sviluppo.

```ts
// src/lib/config.ts
declare global {
  interface Window {
    __APP_CONFIG__?: {
      backendUrl:       string;
      keycloakUrl:      string;
      keycloakRealm:    string;
      keycloakClientId: string;
    };
  }
}

function getConfig() {
  const runtime = typeof window !== 'undefined' ? window.__APP_CONFIG__ : undefined;

  return {
    backend: {
      url: runtime?.backendUrl ?? import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8081',
    },
    keycloak: {
      url:      runtime?.keycloakUrl      ?? import.meta.env.VITE_KEYCLOAK_URL      ?? 'http://localhost:8080',
      realm:    runtime?.keycloakRealm    ?? import.meta.env.VITE_KEYCLOAK_REALM    ?? 'my-realm',
      clientId: runtime?.keycloakClientId ?? import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'my-client',
    },
  } as const;
}

export const config = getConfig();
```

**`.env` (sviluppo locale — non committare)**
```
VITE_BACKEND_URL=http://localhost:8081
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=my-realm
VITE_KEYCLOAK_CLIENT_ID=my-client
```

**`entrypoint.sh` (container Docker)**
```bash
#!/bin/sh
# Genera /config.js a runtime leggendo le env var del container
cat > /usr/share/nginx/html/config.js <<EOF
window.__APP_CONFIG__ = {
  backendUrl:       "${BACKEND_URL}",
  keycloakUrl:      "${KEYCLOAK_URL}",
  keycloakRealm:    "${KEYCLOAK_REALM}",
  keycloakClientId: "${KEYCLOAK_CLIENT_ID}"
};
EOF
exec nginx -g 'daemon off;'
```

**`app.html`** — includere prima del bundle:
```html
<script src="/config.js"></script>
```

---

## apiFetch — wrapper HTTP generico

Un unico punto di uscita verso il backend. Gestisce header `Authorization`, body JSON e status code.

```ts
// src/lib/api/api.ts
import { keycloak } from '$lib/auth/keycloak';
import { config }   from '$lib/config';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

const BASE_URL = `${config.backend.url}/api/v1`;

export async function apiFetch<TResponse = void, TBody = unknown>(
  path: string,
  options: {
    method?:  HttpMethod;
    body?:    TBody;
    headers?: Record<string, string>;
    auth?:    boolean;
  } = {}
): Promise<TResponse> {
  const { method = 'GET', body, headers = {}, auth = false } = options;

  const reqHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...headers,
  };

  if (auth) {
    // Leggere keycloak.token (sempre aggiornato dopo ogni refresh automatico)
    // con fallback a sessionStorage per la prima richiesta dopo F5
    const token = keycloak.token ?? sessionStorage.getItem('access_token');
    if (token) reqHeaders['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: reqHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) await throwApiError(res);     // lancia Error con messaggio dal body
  if (res.status === 204) return undefined as TResponse;
  return res.json() as Promise<TResponse>;
}
```

> **Regola**: leggere sempre `keycloak.token` (live), non `sessionStorage.getItem('access_token')` come fonte primaria. Il valore in sessionStorage può essere scaduto se il refresh è avvenuto a runtime.

---

## keycloak.ts — singleton e auth flow

```ts
// src/lib/auth/keycloak.ts
import Keycloak from 'keycloak-js';
import { config }    from '$lib/config';
import { authStore } from '$lib/stores/authStore';

export const keycloak = new Keycloak({
  url:      config.keycloak.url,
  realm:    config.keycloak.realm,
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
    // Refresh token presente → keycloak-js ripristina la sessione.
    // Se l'access token è scaduto lo refresha automaticamente.
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
    // Nessun token → init passivo, zero redirect automatici
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
  clearTokens();
  authStore.clear();
  keycloak.logout({ redirectUri: window.location.origin });
}

/** Ruoli realm sempre UPPERCASE — consistenza con @RolesAllowed backend */
export function getRoles(): string[] {
  return (keycloak.tokenParsed?.realm_access?.roles ?? []).map(r => r.toUpperCase());
}

/** Route di destinazione post-login in base al ruolo */
export function getPostLoginRoute(): string {
  const roles = getRoles();
  if (roles.includes('ADMIN')) return '/admin';
  if (roles.includes('USER'))  return '/dashboard';
  return '/pending';
}

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
      logout(); // refresh token scaduto → forza logout
    }
  };
  keycloak.onAuthLogout = () => {
    clearTokens();
    authStore.clear();
  };
}

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
      roles:     getRoles(),
    },
  });
}
```

**Perché `checkLoginIframe: false`** — i browser moderni bloccano i cookie di terze parti usati dall'iframe di Keycloak per il silent SSO check.

**Perché `initPromise`** — layout e pagina chiamano entrambi `initKeycloak()` all'`onMount`. Senza Promise condivisa, il secondo invocante riceverebbe `keycloak.authenticated = undefined` e tornerebbe `false`.

---

## authStore.ts

```ts
import { writable } from 'svelte/store';

export interface AuthUser {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

export interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: AuthUser | null;
}

function createAuthStore() {
  const { subscribe, set, update } = writable<AuthState>({
    isAuthenticated: false,
    isLoading: true,
    user: null,
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

## +layout.svelte — auth guard globale + navbar

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { goto, afterNavigate } from '$app/navigation';
  import { initKeycloak, logout } from '$lib/auth/keycloak';
  import { authStore } from '$lib/stores/authStore';

  const PUBLIC_ROUTES = ['/', '/login', '/register', '/pending'];
  let currentPath = '/';

  // afterNavigate aggiorna currentPath ad ogni navigazione SPA
  afterNavigate(({ to }) => {
    currentPath = to?.url.pathname ?? window.location.pathname;
  });

  onMount(async () => {
    currentPath = window.location.pathname;
    const isPublic = PUBLIC_ROUTES.some(r => currentPath === r);

    // initKeycloak() va chiamato ANCHE sulle route pubbliche —
    // altrimenti authStore.isLoading resta true e la navbar non mostra mai i bottoni.
    const authenticated = await initKeycloak();

    if (!authenticated) {
      authStore.setUnauthenticated();
      if (!isPublic) goto('/login', { replaceState: true });
    }
  });
</script>
```

---

## login/+page.svelte — post-login redirect

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { initKeycloak, login, getPostLoginRoute } from '$lib/auth/keycloak';
  import { authStore } from '$lib/stores/authStore';

  let checking = true;

  onMount(async () => {
    try {
      const authenticated = await initKeycloak();
      if (authenticated) {
        goto(getPostLoginRoute(), { replaceState: true });
        return;
      }
      authStore.setUnauthenticated();
    } catch {
      authStore.setUnauthenticated();
    } finally {
      checking = false;
    }
  });
</script>

<!-- redirectUri = /login per ricevere il codice OAuth su questa pagina -->
<button on:click={() => login(`${window.location.origin}/login`)}>
  Accedi
</button>
```

---

## Protezione route (guard pattern)

Ogni pagina protetta esegue questo pattern in `onMount`:

```svelte
<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { initKeycloak, getRoles } from '$lib/auth/keycloak';

  onMount(async () => {
    await initKeycloak();

    if (!getRoles().includes('ADMIN')) {
      goto('/', { replaceState: true });
      return;
    }

    // carica dati specifici della pagina...
  });
</script>
```

---

## Self-action protection — admin non può agire su se stesso

Nelle pagine di gestione utenti, l'admin loggato vede la propria card ma non può eseguire azioni su se stesso.  
Usare `keycloak.subject` (claim `sub` del JWT) come identificatore dell'utente corrente.

```svelte
<script lang="ts">
  import { keycloak } from '$lib/auth/keycloak';

  let currentUserId = '';

  onMount(async () => {
    await initKeycloak();
    currentUserId = keycloak.subject ?? '';
    // ...
  });
</script>

{#each users as user (user.id)}
  {@const isCurrentUser = user.id === currentUserId}

  <!-- Badge visivo -->
  {#if isCurrentUser}
    <span>Tu</span>
  {/if}

  <!-- Tutti i bottoni di azione disabilitati -->
  <button
    disabled={isCurrentUser}
    title={isCurrentUser ? 'Non puoi modificare il tuo account' : undefined}
  >
    Cambia ruoli
  </button>
{/each}
```

> **Regola**: il confronto va fatto su `user.id === keycloak.subject` (UUID Keycloak), non su username o email che potrebbero non essere univoci o potrebbero cambiare.

---

## Paginazione — interfacce riutilizzabili

```ts
// Risposta generica paginata dal backend
export interface PagedResult<T> {
  list:       T[];
  totalRows:  number;
  totalPages: number;
  pageSize:   number;
  page:       number;  // 1-based
}

// Parametri di query paginata
export interface SearchParams {
  page?:       number;  // minimo 1
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

> **Attenzione**: Quarkus usa `@Min(1)` sul parametro `page` — il FE non deve mai inviare `page=0`.

---

## Keycloak — configurazione client (Admin Console)

| Campo | Valore |
|---|---|
| Client type | OpenID Connect |
| Client authentication | **OFF** (public client — niente secret) |
| Standard flow | ON |
| Direct access grants | OFF |
| Valid redirect URIs | `http://localhost:5173/*` (+ URL produzione) |
| Web origins | `http://localhost:5173` (+ URL produzione) |
| Realm roles | UPPERCASE (es. `ADMIN`, `USER`, `PENDING`) |

**Mapper ruoli**: aggiungere un mapper di tipo "User Realm Role" sull'ID token e sull'access token, con token claim name `realm_access.roles`.

---

## Backend Quarkus — configurazione OIDC

```properties
quarkus.oidc.auth-server-url=http://localhost:8080/realms/my-realm
quarkus.oidc.client-id=my-client
quarkus.oidc.application-type=service
quarkus.oidc.roles.role-claim-path=realm_access/roles
```

---

## DTO — pattern di interfacce TypeScript

Le interfacce TS devono replicare esattamente i campi del DTO Java (no campi extra, no rinomina).

```ts
// Corrisponde a SimpleKeycloakUserDTO.java
interface UserDTO {
  id:        string;        // UUID Keycloak (keycloak.subject)
  username:  string;
  firstName: string;
  lastName:  string;
  enabled:   boolean;
  roles:     RoleDTO[];
}

interface RoleDTO {
  id:    string;
  label: string;
}

// Corpo richiesta approvazione — campo dal DTO Java, non array diretto
POST /admin/users/{id}/approve
body: { approvedRoles: string[] }  // ← nome campo esatto del DTO Java
```

---

## Gotcha & lezioni imparate

| Problema | Causa | Soluzione |
|---|---|---|
| Bottoni navbar non appaiono su route pubbliche | `authStore.isLoading` resta `true` | Chiamare `initKeycloak()` anche sulle route pubbliche |
| Race condition layout/pagina | Doppio `init()` su keycloak-js | Usare `initPromise` condivisa (singleton pattern) |
| Sessione persa al F5 | `keycloak.init()` senza token | Passare `token` + `refreshToken` da sessionStorage a `init()` |
| `checkLoginIframe` causa errori | Browser blocca cookie 3p | Impostare sempre `checkLoginIframe: false` |
| 403 Forbidden dal backend | Case mismatch sui ruoli | Normalizzare a UPPERCASE in `getRoles()`; backend usa `@RolesAllowed("ADMIN")` |
| `apiFetch` manda token scaduto | Lettura da sessionStorage (stale) | Leggere `keycloak.token` (live) come fonte primaria |
| `page=0` → 400 Bad Request | Backend `@Min(1)` | Il FE parte sempre da `page=1`, non `page=0` |
| Body array → 400 Bad Request | Backend si aspetta oggetto | Wrappare array in oggetto: `{ approvedRoles: ids }` non `ids` |
| `$app/stores` deprecato in SvelteKit 2 | API rimossa | Usare `afterNavigate` per tracciare il path corrente |
| Admin si auto-disabilita/elimina | Nessuna protezione | Confrontare `user.id === keycloak.subject` e disabilitare i bottoni |
