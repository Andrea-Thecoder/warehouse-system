package it.warehouse.administrator.service;

import io.quarkus.security.UnauthorizedException;
import it.warehouse.administrator.client.KeycloakTokenClient;
import it.warehouse.administrator.dto.LoginRequestDTO;
import it.warehouse.administrator.dto.RefreshTokenRequestDTO;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.TokenResponseDTO;
import it.warehouse.administrator.exception.ServiceException;
import it.warehouse.administrator.model.UserRegistration;
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

    @Inject
    UserRegistrationService userRegistrationService;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-secret")
    String clientSecret;

    public void register(RegisterRequestDTO dto){
        userRegistrationService.registerUser(dto);
    }


}