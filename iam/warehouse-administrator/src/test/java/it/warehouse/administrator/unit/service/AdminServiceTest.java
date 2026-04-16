package it.warehouse.administrator.unit.service;


import io.ebean.Database;
import it.warehouse.administrator.dto.PagedResultDTO;
import it.warehouse.administrator.dto.role.SimpleRoleTypeDTO;
import it.warehouse.administrator.dto.search.BaseSearchRequest;
import it.warehouse.administrator.dto.user.SimpleKeycloakUserDTO;
import it.warehouse.administrator.dto.user.SimpleUserRegistrationDTO;
import it.warehouse.administrator.model.RoleType;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import it.warehouse.administrator.service.AdminService;
import it.warehouse.administrator.service.KeycloakService;
import it.warehouse.administrator.service.UserRegistrationService;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    KeycloakService keycloakService;

    @Mock
    UserRegistrationService userRegistrationService;

    @Mock
    Database db;

    @InjectMocks
    @Spy
    AdminService adminService;


    @Test
    @Order(1)
    void findUserRegistration_shouldCallService() {
        BaseSearchRequest request = new BaseSearchRequest();

        PagedResultDTO<SimpleUserRegistrationDTO> expected = mock(PagedResultDTO.class);

        when(userRegistrationService.findRegistrationRequest(request))
                .thenReturn(expected);

        var result = adminService.findUserRegistration(request);

        assertEquals(expected, result);

        verify(userRegistrationService).findRegistrationRequest(request);
    }

    @Test
    @Order(2)
    void handleApprove_shouldApproveAllRoles() {
        UUID id = UUID.randomUUID();

        UserRegistration registration = mock(UserRegistration.class);
        RoleType role = mock(RoleType.class);

        when(role.getId()).thenReturn("ADMIN");

        when(registration.getRequestedRoleType()).thenReturn(List.of(role));
        when(registration.getKeycloakUserId()).thenReturn("kc-id");

        when(userRegistrationService.getRegistrationPendingOrThrow(id))
                .thenReturn(registration);

        Set<String> approvedRoles = Set.of("ADMIN");

        adminService.handleApprove(id, approvedRoles);

        verify(userRegistrationService).updateRegistrationRequest(
                registration,
                RegistrationStatus.APPROVED
        );

        verify(keycloakService).approveUser("kc-id", List.of("ADMIN"));
    }

    @Test
    @Order(3)
    void handleApprove_shouldReturnPartialApproval() {
        UUID id = UUID.randomUUID();

        UserRegistration registration = mock(UserRegistration.class);
        RoleType role = mock(RoleType.class);

        when(role.getId()).thenReturn("ADMIN");

        when(registration.getRequestedRoleType()).thenReturn(List.of(role));
        when(registration.getKeycloakUserId()).thenReturn("kc-id");

        when(userRegistrationService.getRegistrationPendingOrThrow(id))
                .thenReturn(registration);

        Set<String> approvedRoles = Set.of("USER"); // NON valido

        adminService.handleApprove(id, approvedRoles);

        verify(userRegistrationService).updateRegistrationRequest(
                registration,
                RegistrationStatus.PARTIAL_APPROVED
        );

        verify(keycloakService).approveUser("kc-id", List.of());
    }

    @Test
    @Order(4)
    void findKeycloakUser() {
        BaseSearchRequest request = new BaseSearchRequest();
        request.setPage(1);
        request.setSize(10);

        when(keycloakService.fetchUsers(0, 10)).thenReturn(List.of());

        var result = adminService.findKeycloakUser(request);

        assertNotNull(result);


        verify(keycloakService).fetchUsers(0, 10);
    }

    @Test
    @Order(5)
    void handleReject_shouldRejectUser() {
        UUID id = UUID.randomUUID();

        UserRegistration registration = mock(UserRegistration.class);

        when(registration.getKeycloakUserId()).thenReturn("kc-id");

        when(userRegistrationService.getRegistrationPendingOrThrow(id))
                .thenReturn(registration);

        adminService.handleReject(id);

        verify(userRegistrationService).updateRegistrationRequest(
                registration,
                RegistrationStatus.REJECTED
        );

        verify(keycloakService).rejectUser("kc-id");
    }

    @Test
    void changeRole_shouldCallKeycloak() {
        Set<String> roles = Set.of("ADMIN");

        adminService.changeRole("user-id", roles);

        verify(keycloakService).changeRolesForUser("user-id", roles);
    }

    @Test
    void handleEnabledUser_shouldCallKeycloak() {
        adminService.handleEnabledUser("user-id", true);

        verify(keycloakService).handleEnabledUser("user-id", true);
    }

    @Test
    void deleteUser_shouldCallKeycloak() {
        adminService.deleteUser("user-id");

        verify(keycloakService).deleteUser("user-id");
    }

}
