package it.warehouse.administrator.unit.service;

import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.role.SimpleRoleTypeDTO;
import it.warehouse.administrator.exception.ServiceException;
import it.warehouse.administrator.service.KeycloakService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KeycloakServiceTest extends KeycloakMockTest {


    @BeforeEach
    void injectRealm() throws Exception {
        Field realmField = KeycloakService.class.getDeclaredField("realm");
        realmField.setAccessible(true);
        realmField.set(keycloakService, "test-realm");
    }

    @Test
    @Order(1)
    void createUserAccount_success_shouldReturnKeycloakId() {
        Response response = mockResponse(201, URI.create("http://keycloak/auth/admin/realms/test/users/new-kc-id"));
        when(usersResource.create(any())).thenReturn(response);

        String result = keycloakService.createUserAccount(buildRegisterDto());

        assertEquals("new-kc-id", result);
        verify(usersResource).create(any());
    }

    @Test
    @Order(2)
    void createUserAccount_conflict_shouldThrowServiceException() {
        Response response = mockResponse(409, null);  // ← prima
        when(usersResource.create(any())).thenReturn(response);  // ← poi

        ServiceException ex = assertThrows(ServiceException.class,
                () -> keycloakService.createUserAccount(buildRegisterDto()));

        assertEquals("Username or email already in use", ex.getMessage());
    }

    @Test
    @Order(3)
    void createUserAccount_serverError_shouldThrowServiceException() {
        Response response = mockResponse(500, null);  // ← prima
        when(usersResource.create(any())).thenReturn(response);  // ← poi

        ServiceException ex = assertThrows(ServiceException.class,
                () -> keycloakService.createUserAccount(buildRegisterDto()));

        assertEquals("Error while creating the user", ex.getMessage());
    }

    @Test
    @Order(4)
    void approveUser_success_shouldEnableUserAndAssignRoles() {
        keycloakService.approveUser("kc-id", List.of("ADMIN"));


        verify(userResource).update(any(UserRepresentation.class));
        verify(roleScopeResource).add(anyList());
    }

    @Test
    @Order(5)
    void approveUser_keycloakError_shouldThrowWrappedServiceException() {
        when(realmResource.users()).thenThrow(new RuntimeException("Keycloak unreachable"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> keycloakService.approveUser("kc-id", List.of("ADMIN")));

        assertTrue(ex.getMessage().contains("Error while approving the user on Keycloak"));
    }

    @Test
    @Order(6)
    void rejectUser_success_shouldDeleteUserFromKeycloak() {
        keycloakService.rejectUser("kc-id");

        verify(usersResource).delete("kc-id");
    }

    @Test
    @Order(7)
    void rejectUser_keycloakError_shouldThrowWrappedServiceException() {
        when(usersResource.delete(anyString())).thenThrow(new RuntimeException("Keycloak unreachable"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> keycloakService.rejectUser("kc-id"));

        assertTrue(ex.getMessage().contains("Error while deleting the user"));
    }

    @Test
    @Order(8)
    void getUserById_shouldReturnUserRepresentationFromKeycloak() {
        UserRepresentation result = keycloakService.getUserById("kc-id");

        assertNotNull(result);
        assertEquals("username", result.getUsername());
        verify(userResource).toRepresentation();
    }

    @Test
    @Order(9)
    void fetchUsers_shouldDelegateToKeycloakWithCorrectPagination() {
        List<UserRepresentation> result = keycloakService.fetchUsers(0, 20);

        assertNotNull(result);
        verify(usersResource).list(0, 20);
    }

    @Test
    @Order(10)
    void getRolesFromUser_shouldReturnOnlyRolesPresentInRolesMap() {
        when(lookupService.getRolesMap()).thenReturn(Map.of("ADMIN", "Amministratore"));
        userRepresentation.setId("kc-id");

        List<SimpleRoleTypeDTO> roles = keycloakService.getRolesFromUser(userRepresentation);

        assertEquals(1, roles.size());
        assertEquals("ADMIN", roles.getFirst().getId());
        assertEquals("Amministratore", roles.getFirst().getLabel());
    }

    @Test
    @Order(11)
    void getRolesFromUser_shouldReturnEmpty_whenNoRoleMatchesMap() {
        when(lookupService.getRolesMap()).thenReturn(Map.of());

        List<SimpleRoleTypeDTO> roles = keycloakService.getRolesFromUser(userRepresentation);

        assertTrue(roles.isEmpty());
    }

    @Test
    @Order(12)
    void changeRolesForUser_shouldRemoveCurrentRolesAndAssignNew() {
        when(lookupService.getRolesMap()).thenReturn(Map.of("ADMIN", "Amministratore"));

        keycloakService.changeRolesForUser("kc-id", Set.of("ADMIN"));

        verify(roleScopeResource).remove(anyList());
        verify(roleScopeResource).add(anyList());
    }

    @Test
    @Order(13)
    void changeRolesForUser_shouldIgnoreRolesNotPresentInRolesMap() {
        when(lookupService.getRolesMap()).thenReturn(Map.of("ADMIN", "Amministratore"));

        keycloakService.changeRolesForUser("kc-id", Set.of("UNKNOWN_ROLE"));

        verify(roleScopeResource).remove(anyList());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoleRepresentation>> captor = ArgumentCaptor.forClass(List.class);
        verify(roleScopeResource).add(captor.capture());
        assertTrue(captor.getValue().isEmpty());
    }


    @Test
    @Order(14)
    void handleEnabledUser_enable_shouldUpdateUserWithEnabledTrue() {
        keycloakService.handleEnabledUser("kc-id", true);

        ArgumentCaptor<UserRepresentation> captor = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(userResource).update(captor.capture());
        assertTrue(captor.getValue().isEnabled());
    }

    @Test
    @Order(15)
    void handleEnabledUser_disable_shouldUpdateUserWithEnabledFalse() {
        keycloakService.handleEnabledUser("kc-id", false);

        ArgumentCaptor<UserRepresentation> captor = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(userResource).update(captor.capture());
        assertFalse(captor.getValue().isEnabled());
    }

    @Test
    @Order(16)
    void deleteUser_shouldDelegateToRejectUser() {
        keycloakService.deleteUser("kc-id");
        verify(usersResource).delete("kc-id");
    }

    private Response mockResponse(int status, URI location) {
        Response response = mock(Response.class);
        lenient().when(response.getStatus()).thenReturn(status);
        lenient().when(response.getStatusInfo()).thenReturn(Response.Status.fromStatusCode(status));
        if (location != null) {
            lenient().when(response.getLocation()).thenReturn(location);
        }
        return response;
    }

    private RegisterRequestDTO buildRegisterDto() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("mario.rossi");
        dto.setEmail("mario@example.com");
        dto.setFirstName("Mario");
        dto.setLastName("Rossi");
        dto.setPassword("Password123!");
        dto.setRequestedRoleIds(List.of("ADMIN"));
        return dto;
    }
}
