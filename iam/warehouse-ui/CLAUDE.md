# CLAUDE.md — warehouse-ui

## Panoramica del progetto

Frontend per il sistema di gestione magazzino. SPA moderna costruita con SvelteKit 2 + TypeScript 5 + Tailwind CSS v4, bundling Vite 5.

- **Framework**: SvelteKit 2 (Svelte 4) — modalità **SPA pura, SSR disabilitato**
- **CSS**: Tailwind CSS v4 via `@tailwindcss/vite`
- **Linguaggio**: TypeScript (strict: true)
- **Build**: Vite 5
- **Lingua UI**: Italiano

---

## Regole architetturali fondamentali (NON derogabili)

### SvelteKit è solo client-side

- **Vietato** usare `+page.server.ts`, `+layout.server.ts`, `+server.ts` o qualsiasi file `.server.`
- **Vietato** usare `load` server-side o `actions` di SvelteKit
- SSR è disabilitato globalmente in `src/routes/+layout.ts` (`export const ssr = false`)
- SvelteKit gestisce solo **routing e componenti UI**

### Tutto server-side passa da backend o Keycloak

- Qualsiasi operazione che richiede privilegi server va al **backend REST** o alle **API Keycloak**
- Il FE non deve mai avere credenziali admin o segreti — solo `VITE_` env variables pubbliche
- La registrazione utente: `FE → POST backend → backend crea utente su Keycloak`
- Non chiamare mai la Keycloak Admin API direttamente dal FE

### Autenticazione

- **Library**: `keycloak-js` — OIDC Authorization Code Flow + PKCE, gestito interamente client-side
- **Token storage**: `sessionStorage` (non localStorage, non cookie)
- **Post-login redirect**: gestito dal FE leggendo i ruoli dal JWT decodificato
  - `admin` → `/admin`
  - `user` → `/dashboard`
  - `pending` → `/pending`
- I ruoli arrivano nel JWT come claim (mappati lato Keycloak)

### Keycloak (configurazione attesa)

- Flow: Standard Flow (Authorization Code + PKCE, S256)
- Client type: Public (niente client secret esposto al FE)
- Valid Redirect URIs: configurate per ogni ambiente
- Ruoli applicativi: `admin`, `user`, `pending`
- Mapper: ruoli inclusi nell'ID token e/o access token

---

## Comandi essenziali

```bash
npm run dev          # server di sviluppo con hot reload
npm run build        # build per produzione (output statico)
npm run preview      # anteprima build
npm run check        # type-check TypeScript + Svelte
npm run check:watch  # type-check in watch mode
```

---

## Struttura src/

```
src/
├── app.html                  # template HTML root (lang="it")
├── app.css                   # @import tailwindcss + design tokens
├── app.d.ts                  # tipi globali App namespace
├── lib/
│   ├── config.ts             # costanti env (VITE_ vars): backendUrl, keycloak config
│   ├── api/
│   │   └── auth.ts           # chiamate HTTP al backend (register, ecc.)
│   ├── auth/
│   │   └── keycloak.ts       # inizializzazione e helpers keycloak-js
│   ├── stores/               # Svelte stores (authStore, ecc.)
│   └── components/           # componenti UI riutilizzabili
└── routes/
    ├── +layout.ts            # ssr = false (globale)
    ├── +layout.svelte        # navbar + slot
    ├── +page.svelte          # homepage pubblica
    ├── register/
    │   └── +page.svelte      # form registrazione → chiama backend
    ├── login/
    │   └── +page.svelte      # trigger keycloak redirect
    ├── pending/
    │   └── +page.svelte      # "account in attesa di approvazione"
    ├── dashboard/            # protetta: role=user
    └── admin/                # protetta: role=admin
```

---

## Convenzioni di codice

- **Niente file `.server.`** — mai, in nessun caso
- **Variabili d'ambiente**: solo prefisso `VITE_` (es. `VITE_BACKEND_URL`, `VITE_KEYCLOAK_URL`)
- **Import alias**: `$lib` per `src/lib/`
- **Moduli ESM**: tutto `import`/`export`, niente `require`
- **TypeScript strict**: attivo — tutti i tipi devono essere espliciti
- **Testi utente**: in italiano

---

## Cose da NON fare

- ❌ Creare file `*.server.ts` o `*.server.js`
- ❌ Usare `import { ... } from '$app/server'`
- ❌ Chiamare Keycloak Admin API dal FE
- ❌ Salvare token in localStorage (usare sessionStorage)
- ❌ Committare `.env` o file con credenziali
- ❌ Modificare `.svelte-kit/` (generato automaticamente)
- ❌ Aggiungere `node_modules/` al git
