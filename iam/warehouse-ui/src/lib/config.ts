/**
 * Configurazione centralizzata dell'applicazione.
 *
 * In produzione (container) i valori vengono iniettati a runtime da
 * /config.js (generato da entrypoint.sh) tramite window.__APP_CONFIG__.
 * Kubernetes li fornisce via ConfigMap/Secret senza dover ribuilare l'immagine.
 *
 * In sviluppo locale i fallback leggono le variabili VITE_ dal file .env.
 */

declare global {
  interface Window {
    __APP_CONFIG__?: {
      backendUrl: string;
      keycloakUrl: string;
      keycloakRealm: string;
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
      realm:    runtime?.keycloakRealm    ?? import.meta.env.VITE_KEYCLOAK_REALM    ?? 'warehouse-realm',
      clientId: runtime?.keycloakClientId ?? import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'warehouse-ui',
    },
  } as const;
}

export const config = getConfig();
