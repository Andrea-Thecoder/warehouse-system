# ─────────────────────────────────────────────
# Stage 1 — Build
# ─────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Installa dipendenze (layer cacheable separato dai sorgenti)
COPY package*.json ./
RUN npm ci

# Copia sorgenti e compila
# Le VITE_ non servono più a build time: la config arriva a runtime via config.js
COPY . .
RUN npm run build

# ─────────────────────────────────────────────
# Stage 2 — Serve (nginx)
# ─────────────────────────────────────────────
FROM nginx:1.27-alpine AS runtime

# Rimuove la configurazione default di nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia configurazione nginx per SPA
COPY nginx.conf /etc/nginx/conf.d/app.conf

# Copia i file statici generati dal build
COPY --from=builder /app/build /usr/share/nginx/html

# Copia l'entrypoint che genera config.js a runtime
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Permessi per utente non-root
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown nginx:nginx /var/run/nginx.pid

USER nginx

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost:8080/health || exit 1

# Env var con valori di default (override via ConfigMap in Kubernetes)
ENV BACKEND_URL=http://localhost:8081 \
    KEYCLOAK_URL=http://localhost:8080 \
    KEYCLOAK_REALM=warehouse-realm \
    KEYCLOAK_CLIENT_ID=warehouse-ui

ENTRYPOINT ["/entrypoint.sh"]
