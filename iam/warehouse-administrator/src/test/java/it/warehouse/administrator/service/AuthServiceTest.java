package it.warehouse.administrator.service;

import it.warehouse.administrator.client.KeycloakTokenClient;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KeycloakTokenClient tokenClient;

    @Mock
    UserRegistrationService userRegistrationService;

    @InjectMocks
    private AuthService authService;


    @BeforeEach
    void setUp() throws Exception {
        injectField(authService, "tokenClient", tokenClient);
        injectField(authService, "realm",        "warehouse-realm");
        injectField(authService, "clientId",     "warehouse-administrator");
        injectField(authService, "clientSecret", "test-secret");
    }


    @Test
    void register_delegatesToUserRegistrationService() {
        RegisterRequestDTO dto = new RegisterRequestDTO();

        authService.register(dto);

        verify(userRegistrationService).registerUser(dto);
    }




    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}