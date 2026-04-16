package it.warehouse.administrator.unit.resource;

import it.warehouse.administrator.api.AuthResource;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.SimpleResultDTO;
import it.warehouse.administrator.service.AuthService;
import it.warehouse.administrator.service.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthResourceTest {

    @Mock
    AuthService authService;

    @InjectMocks
    AuthResource authResource;


    @Test
    public void createNewUserTest(){
        RegisterRequestDTO  registerRequestDTO = mock(RegisterRequestDTO.class);
        doNothing().when(authService).register(registerRequestDTO);

        var result = authResource.register(registerRequestDTO);

        assertNotNull(result);
        assertNotNull(result.getMessage());
        verify(authService, times(1)).register(registerRequestDTO);
    }

}
