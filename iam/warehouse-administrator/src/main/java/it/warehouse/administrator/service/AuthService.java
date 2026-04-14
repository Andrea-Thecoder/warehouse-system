package it.warehouse.administrator.service;

import io.quarkus.security.UnauthorizedException;
import it.warehouse.administrator.client.KeycloakTokenClient;
import it.warehouse.administrator.dto.LoginRequestDTO;
import it.warehouse.administrator.dto.RefreshTokenRequestDTO;
import it.warehouse.administrator.dto.TokenResponseDTO;
import it.warehouse.administrator.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@Slf4j
public class AuthService {

    @Inject
    @RestClient
    KeycloakTokenClient tokenClient;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-secret")
    String clientSecret;

    public TokenResponseDTO login(LoginRequestDTO dto) {
        log.info("Tentativo di login per l'utente: {}", dto.getUsername());
        MultivaluedHashMap<String, String> form = new MultivaluedHashMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", dto.getUsername());
        form.add("password", dto.getPassword());

        try {
            return tokenClient.token(form);
        } catch (WebApplicationException e) {
            log.error("Login fallito per l'utente {}: status {}", dto.getUsername(), e.getResponse().getStatus());
            throw new UnauthorizedException("Credenziali non valide");
        }
    }

    public TokenResponseDTO refresh(RefreshTokenRequestDTO dto) {
        log.debug("Richiesta refresh token");
        MultivaluedHashMap<String, String> form = new MultivaluedHashMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", dto.getRefreshToken());

        try {
            return tokenClient.token(form);
        } catch (WebApplicationException e) {
            log.error("Refresh token non valido: status {}", e.getResponse().getStatus());
            throw new UnauthorizedException("Refresh token non valido o scaduto");
        }
    }

    public void logout(RefreshTokenRequestDTO dto) {
        log.info("Logout richiesto");
        MultivaluedHashMap<String, String> form = new MultivaluedHashMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", dto.getRefreshToken());

        try {
            tokenClient.logout(form);
        } catch (WebApplicationException e) {
            log.error("Logout fallito: status {}", e.getResponse().getStatus());
            throw new ServiceException("Logout non riuscito");
        }
    }
}