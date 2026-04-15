package it.warehouse.administrator.service;

import io.ebean.Database;
import it.warehouse.administrator.dto.PagedResultDTO;
import it.warehouse.administrator.dto.role.SimpleRoleTypeDTO;
import it.warehouse.administrator.dto.search.BaseSearchRequest;
import it.warehouse.administrator.dto.user.SimpleKeycloakUserDTO;
import it.warehouse.administrator.dto.user.SimpleUserRegistrationDTO;
import it.warehouse.administrator.model.RoleType;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import it.warehouse.administrator.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private KeycloakService keycloakService;
    @Mock
    private UserService userService;
    @Mock
    private Database db;

    @InjectMocks
    private AdminService adminService;

    // -------------------------------------------------------------------------
    // handleApprove
    // -------------------------------------------------------------------------

    @Test
    void handleApprove_allRolesGranted_setsStatusApproved() {
        UUID id = UUID.randomUUID();
        UserRegistration reg = mock(UserRegistration.class);
        when(reg.getRequestedRoleType()).thenReturn(List.of(roleType("ADMIN"), roleType("WAREHOUSE_OPERATOR")));
        when(reg.getKeycloakUserId()).thenReturn("kc-123");
        when(userService.getRegistrationPendingOrThrow(id)).thenReturn(reg);

        adminService.handleApprove(id, Set.of("ADMIN", "WAREHOUSE_OPERATOR"));

        verify(userService).updateRegistrationRequest(reg, RegistrationStatus.APPROVED);
        verify(keycloakService).approveUser(eq("kc-123"), argThat(roles -> roles.containsAll(List.of("ADMIN", "WAREHOUSE_OPERATOR"))));
    }

    @Test
    void handleApprove_partialRolesGranted_setsStatusPartialApproved() {
        UUID id = UUID.randomUUID();
        UserRegistration reg = mock(UserRegistration.class);
        when(reg.getRequestedRoleType()).thenReturn(List.of(roleType("ADMIN"), roleType("WAREHOUSE_OPERATOR")));
        when(reg.getKeycloakUserId()).thenReturn("kc-123");
        when(userService.getRegistrationPendingOrThrow(id)).thenReturn(reg);

        adminService.handleApprove(id, Set.of("ADMIN")); // solo 1 dei 2 ruoli richiesti

        verify(userService).updateRegistrationRequest(reg, RegistrationStatus.PARTIAL_APPROVED);
        verify(keycloakService).approveUser(eq("kc-123"), argThat(roles -> roles.size() == 1 && roles.contains("ADMIN")));
    }

    @Test
    void handleApprove_approvedRolesNotInRequestedOnes_setsStatusPartialApproved() {
        UUID id = UUID.randomUUID();
        UserRegistration reg = mock(UserRegistration.class);
        when(reg.getRequestedRoleType()).thenReturn(List.of(roleType("ADMIN"), roleType("WAREHOUSE_OPERATOR")));
        when(reg.getKeycloakUserId()).thenReturn("kc-123");
        when(userService.getRegistrationPendingOrThrow(id)).thenReturn(reg);

        // ruolo approvato non fa parte di quelli richiesti → validRoles sarà vuoto
        adminService.handleApprove(id, Set.of("TRANSPORT_ADMIN"));

        verify(userService).updateRegistrationRequest(reg, RegistrationStatus.PARTIAL_APPROVED);
        verify(keycloakService).approveUser(eq("kc-123"), argThat(List::isEmpty));
    }

    // -------------------------------------------------------------------------
    // handleReject
    // -------------------------------------------------------------------------

    @Test
    void handleReject_updatesStatusToRejectedAndDeletesUserFromKeycloak() {
        UUID id = UUID.randomUUID();
        UserRegistration reg = mock(UserRegistration.class);
        when(reg.getKeycloakUserId()).thenReturn("kc-456");
        when(userService.getRegistrationPendingOrThrow(id)).thenReturn(reg);

        adminService.handleReject(id);

        verify(userService).updateRegistrationRequest(reg, RegistrationStatus.REJECTED);
        verify(keycloakService).rejectUser("kc-456");
    }

    // -------------------------------------------------------------------------
    // changeRole
    // -------------------------------------------------------------------------

    @Test
    void changeRole_delegatesToKeycloakService() {
        Set<String> roles = Set.of("WAREHOUSE_ADMIN");

        adminService.changeRole("kc-user-1", roles);

        verify(keycloakService).changeRolesForUser("kc-user-1", roles);
    }

    // -------------------------------------------------------------------------
    // handleEnabledUser
    // -------------------------------------------------------------------------

    @Test
    void handleEnabledUser_enable_delegatesToKeycloakService() {
        adminService.handleEnabledUser("kc-user-1", true);
        verify(keycloakService).handleEnabledUser("kc-user-1", true);
    }

    @Test
    void handleEnabledUser_disable_delegatesToKeycloakService() {
        adminService.handleEnabledUser("kc-user-1", false);
        verify(keycloakService).handleEnabledUser("kc-user-1", false);
    }

    // -------------------------------------------------------------------------
    // deleteUser
    // -------------------------------------------------------------------------

    @Test
    void deleteUser_delegatesToKeycloakService() {
        adminService.deleteUser("kc-user-1");
        verify(keycloakService).deleteUser("kc-user-1");
    }

    // -------------------------------------------------------------------------
    // findUserRegistration
    // -------------------------------------------------------------------------

    @Test
    void findUserRegistration_delegatesToUserService() {
        BaseSearchRequest req = new BaseSearchRequest();
        PagedResultDTO<SimpleUserRegistrationDTO> expected = new PagedResultDTO<>();
        when(userService.findRegistrationRequest(req)).thenReturn(expected);

        PagedResultDTO<SimpleUserRegistrationDTO> result = adminService.findUserRegistration(req);

        assertThat(result).isSameAs(expected);
    }

    // -------------------------------------------------------------------------
    // findKeycloakUser
    // -------------------------------------------------------------------------

    @Test
    void findKeycloakUser_returnsMappedDtos() {
        BaseSearchRequest req = new BaseSearchRequest();
        req.setPage(1);
        req.setSize(20);

        UserRepresentation user = new UserRepresentation();
        user.setId("kc-1");
        user.setUsername("mario.rossi");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setEnabled(true);

        List<SimpleRoleTypeDTO> roles = List.of(new SimpleRoleTypeDTO("ADMIN", "Admin"));
        when(keycloakService.fetchUsers(0, 20)).thenReturn(List.of(user));
        when(keycloakService.getRolesFromUser(user)).thenReturn(roles);

        PagedResultDTO<SimpleKeycloakUserDTO> result = adminService.findKeycloakUser(req);

        assertThat(result.getList()).hasSize(1);
        SimpleKeycloakUserDTO dto = result.getList().get(0);
        assertThat(dto.getId()).isEqualTo("kc-1");
        assertThat(dto.getUsername()).isEqualTo("mario.rossi");
        assertThat(dto.getRoles()).hasSize(1);
        assertThat(dto.getRoles().get(0).getId()).isEqualTo("ADMIN");
    }

    @Test
    void findKeycloakUser_emptyList_returnsEmptyResult() {
        BaseSearchRequest req = new BaseSearchRequest();
        req.setPage(1);
        req.setSize(20);
        when(keycloakService.fetchUsers(0, 20)).thenReturn(List.of());

        PagedResultDTO<SimpleKeycloakUserDTO> result = adminService.findKeycloakUser(req);

        assertThat(result.getList()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private RoleType roleType(String id) {
        RoleType rt = new RoleType();
        rt.setId(id);
        rt.setLabel(id);
        return rt;
    }
}