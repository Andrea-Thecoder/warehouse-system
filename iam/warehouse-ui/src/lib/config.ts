/**
 * Configurazione centralizzata dell'applicazione.
 * Tutte le variabili d'ambiente sono VITE_ (esposte al client, mai segreti).
 *
 * Crea un file .env nella root del progetto:
 *   VITE_BACKEND_URL=http://localhost:8081
 *   VITE_KEYCLOAK_URL=http://localhost:9090
 *   VITE_KEYCLOAK_REALM=warehouse
 *   VITE_KEYCLOAK_CLIENT_ID=warehouse-ui
 */

export const config = {
  backend: {
    url: import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8081',
  },
  keycloak: {
    url:      import.meta.env.VITE_KEYCLOAK_URL      ?? 'http://localhost:8080',
    realm:    import.meta.env.VITE_KEYCLOAK_REALM    ?? 'warehouse-realm',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'warehouse-ui',
  },
} as const;
