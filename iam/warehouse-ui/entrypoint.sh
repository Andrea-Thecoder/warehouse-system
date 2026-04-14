#!/bin/sh
set -e

# Genera /usr/share/nginx/html/config.js a runtime leggendo le env var del container.
# Kubernetes inietterà questi valori via ConfigMap/Secret.
# I fallback sono gli stessi presenti in config.ts per sviluppo locale.

cat > /usr/share/nginx/html/config.js << EOF
window.__APP_CONFIG__ = {
  backendUrl:      "${BACKEND_URL:-http://localhost:8081}",
  keycloakUrl:     "${KEYCLOAK_URL:-http://localhost:8080}",
  keycloakRealm:   "${KEYCLOAK_REALM:-warehouse-realm}",
  keycloakClientId:"${KEYCLOAK_CLIENT_ID:-warehouse-ui}"
};
EOF

echo "[entrypoint] config.js generato:"
cat /usr/share/nginx/html/config.js

exec nginx -g "daemon off;"
