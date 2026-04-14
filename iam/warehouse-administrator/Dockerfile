# =============================================================================
# Stage 1 — Build
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Scarica le dipendenze in cache separata rispetto al codice sorgente
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# =============================================================================
# Stage 2 — Runtime
# =============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Utente non-root per sicurezza
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia il fast-jar di Quarkus (struttura quarkus-app/)
COPY --from=builder --chown=appuser:appgroup /app/target/quarkus-app /app

EXPOSE 8081

# Ottimizzazioni JVM per ambienti containerizzati
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.net.preferIPv4Stack=true \
               -Dquarkus.profile=prod"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/quarkus-run.jar"]