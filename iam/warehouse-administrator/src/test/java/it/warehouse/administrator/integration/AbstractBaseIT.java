package it.warehouse.administrator.integration;

import io.ebean.Database;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import it.warehouse.administrator.dto.role.SimpleRoleTypeDTO;
import it.warehouse.administrator.exception.ServiceException;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.service.KeycloakService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@QuarkusTest
public abstract class AbstractBaseIT {

    static {
        RestAssured.basePath = "/api/v1/warehouse-administrator";
    }

    @Inject
    protected Database db;

    @InjectMock
    protected KeycloakService keycloakService;

    @BeforeEach
    void isolate() {
        db.sqlUpdate("DELETE FROM registration_request_role").execute();
        db.find(UserRegistration.class).delete();
        Mockito.reset(keycloakService);
    }

    protected void mockCreateUserAccountOk(String returnedKeycloakId) {
        when(keycloakService.createUserAccount(any())).thenReturn(returnedKeycloakId);
    }

    protected void mockCreateUserAccountThrows() {
        when(keycloakService.createUserAccount(any())).thenThrow(new ServiceException("Error while creating the user"));
    }

    protected void mockApproveUserOk() {
        doNothing().when(keycloakService).approveUser(anyString(), anyList());
    }

    protected void mockApproveUserThrows() {
        doThrow(new ServiceException("Error while approving the user on Keycloak. Try again later."))
                .when(keycloakService).approveUser(anyString(), anyList());
    }

    protected void mockRejectUserOk() {
        doNothing().when(keycloakService).rejectUser(anyString());
    }

    protected void mockRejectUserThrows() {
        doThrow(new ServiceException("Error while deleting the user. Try again later."))
                .when(keycloakService).rejectUser(anyString());
    }

    protected void mockGetUserByIdOk(UserRepresentation user) {
        when(keycloakService.getUserById(anyString())).thenReturn(user);
    }

    protected void mockGetUserByIdThrows() {
        when(keycloakService.getUserById(anyString())).thenThrow(new ServiceException("User not found on Keycloak"));
    }

    protected void mockFetchUsersOk(List<UserRepresentation> users) {
        when(keycloakService.fetchUsers(anyInt(), anyInt())).thenReturn(users);
    }

    protected void mockFetchUsersThrows() {
        when(keycloakService.fetchUsers(anyInt(), anyInt())).thenThrow(new ServiceException("Error while fetching users"));
    }

    protected void mockGetRolesFromUserOk(List<SimpleRoleTypeDTO> roles) {
        when(keycloakService.getRolesFromUser(any())).thenReturn(roles);
    }

    protected void mockGetRolesFromUserThrows() {
        when(keycloakService.getRolesFromUser(any())).thenThrow(new ServiceException("Error while fetching roles"));
    }

    protected void mockChangeRolesForUserOk() {
        doNothing().when(keycloakService).changeRolesForUser(anyString(), any(Set.class));
    }

    protected void mockChangeRolesForUserThrows() {
        doThrow(new ServiceException("Error while changing roles"))
                .when(keycloakService).changeRolesForUser(anyString(), any(Set.class));
    }

    protected void mockHandleEnabledUserOk() {
        doNothing().when(keycloakService).handleEnabledUser(anyString(), anyBoolean());
    }

    protected void mockHandleEnabledUserThrows() {
        doThrow(new ServiceException("Error while enabling/disabling user"))
                .when(keycloakService).handleEnabledUser(anyString(), anyBoolean());
    }


    protected void mockDeleteUserOk() {
        doNothing().when(keycloakService).deleteUser(anyString());
    }

    protected void mockDeleteUserThrows() {
        doThrow(new ServiceException("Error while deleting user"))
                .when(keycloakService).deleteUser(anyString());
    }
}