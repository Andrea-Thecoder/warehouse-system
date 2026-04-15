package it.warehouse.administrator.service;

import io.quarkus.security.UnauthorizedException;
import it.warehouse.administrator.client.KeycloakTokenClient;
import it.warehouse.administrator.dto.LoginRequestDTO;
import it.warehouse.administrator.dto.RefreshTokenRequestDTO;
import it.warehouse.administrator.dto.TokenResponseDTO;
import it.warehouse.administrator.exception.ServiceException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KeycloakTokenClient tokenClient;

    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        authService = new AuthService();
        injectField(authService, "tokenClient", tokenClient);
        injectField(authService, "realm",        "warehouse-realm");
        injectField(authService, "clientId",     "warehouse-administrator");
        injectField(authService, "clientSecret", "test-secret");
    }

    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    void login_validCredentials_returnsTokenResponse() {
        LoginRequestDTO dto = loginDto("mario.rossi", "password123");
        TokenResponseDTO expected = buildToken("access-tok", "refresh-tok");
        when(tokenClient.token(any())).thenReturn(expected);

        TokenResponseDTO result = authService.login(dto);

        assertThat(result.getAccessToken()).isEqualTo("access-tok");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-tok");
        verify(tokenClient).token(argThat(form ->
                "password".equals(form.getFirst("grant_type")) &&
                "mario.rossi".equals(form.getFirst("username"))
        ));
    }

    @Test
    void login_invalidCredentials_throwsUnauthorizedException() {
        LoginRequestDTO dto = loginDto("unknown", "wrong");
        when(tokenClient.token(any()))
                .thenThrow(new WebApplicationException(Response.status(401).build()));

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    // -------------------------------------------------------------------------
    // refresh
    // -------------------------------------------------------------------------

    @Test
    void refresh_validToken_returnsNewTokenResponse() {
        RefreshTokenRequestDTO dto = refreshDto("valid-refresh-token");
        TokenResponseDTO expected = buildToken("new-access", "new-refresh");
        when(tokenClient.token(any())).thenReturn(expected);

        TokenResponseDTO result = authService.refresh(dto);

        assertThat(result).isEqualTo(expected);
        verify(tokenClient).token(argThat(form ->
                "refresh_token".equals(form.getFirst("grant_type")) &&
                "valid-refresh-token".equals(form.getFirst("refresh_token"))
        ));
    }

    @Test
    void refresh_expiredToken_throwsUnauthorizedException() {
        RefreshTokenRequestDTO dto = refreshDto("expired-token");
        when(tokenClient.token(any()))
                .thenThrow(new WebApplicationException(Response.status(401).build()));

        assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(UnauthorizedException.class);
    }

    // -------------------------------------------------------------------------
    // logout
    // -------------------------------------------------------------------------

    @Test
    void logout_validToken_callsKeycloakLogoutEndpoint() {
        RefreshTokenRequestDTO dto = refreshDto("valid-refresh-token");

        authService.logout(dto);

        verify(tokenClient).logout(argThat(form ->
                "valid-refresh-token".equals(form.getFirst("refresh_token"))
        ));
    }

    @Test
    void logout_keycloakFailure_throwsServiceException() {
        RefreshTokenRequestDTO dto = refreshDto("bad-token");
        doThrow(new WebApplicationException(Response.status(500).build()))
                .when(tokenClient).logout(any());

        assertThatThrownBy(() -> authService.logout(dto))
                .isInstanceOf(ServiceException.class);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private LoginRequestDTO loginDto(String username, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    private RefreshTokenRequestDTO refreshDto(String token) {
        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO();
        dto.setRefreshToken(token);
        return dto;
    }

    private TokenResponseDTO buildToken(String access, String refresh) {
        TokenResponseDTO t = new TokenResponseDTO();
        t.setAccessToken(access);
        t.setRefreshToken(refresh);
        t.setExpiresIn(300);
        return t;
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}