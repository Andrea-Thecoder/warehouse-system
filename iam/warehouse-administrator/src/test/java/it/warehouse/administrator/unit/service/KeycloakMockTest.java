package it.warehouse.administrator.unit.service;


import it.warehouse.administrator.service.KeycloakService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakMockTest {

    @Mock
    Keycloak keycloak;

    @InjectMocks
    @Spy
    KeycloakService keycloakService;

    @Mock
    RealmResource realmResource;

    @Mock
    UsersResource usersResource;

    @Mock
    RolesResource rolesResource;

    @Mock
    UserResource userResource;

    @Mock
    RoleResource roleResource;

    @Mock
    RoleMappingResource roleMappingResource;

    @Mock
    RoleScopeResource roleScopeResource;


    UserRepresentation userRepresentation;
    RoleRepresentation roleRepresentation;

    @BeforeEach
    void setup() {
        lenient().when(keycloak.realm(anyString())).thenReturn(realmResource);
        mockUserChain();
        mockUserRep();
        mockRoleRep();
        mockRoleMap();
    }

    private void mockRoleMap() {
        lenient().when(userResource.roles()).thenReturn(roleMappingResource);
        lenient().when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        lenient().when(roleScopeResource.listAll()).thenReturn(List.of(roleRepresentation));
        lenient().doNothing().when(roleScopeResource).add(anyList());
        lenient().doNothing().when(roleScopeResource).remove(anyList());
    }

    private void mockRoleRep() {
        lenient().when(realmResource.roles()).thenReturn(rolesResource);
        lenient().when(rolesResource.get(anyString())).thenReturn(roleResource);
        roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName("ADMIN");
        lenient().when(roleResource.toRepresentation()).thenReturn(roleRepresentation);
    }

    private void mockUserRep() {
        userRepresentation = new UserRepresentation();
        userRepresentation.setId("string");
        userRepresentation.setUsername("username");
        lenient().when(userResource.toRepresentation()).thenReturn(userRepresentation);
        lenient().doNothing().when(userResource).update(any());
    }

    private void mockUserChain() {
        lenient().when(realmResource.users()).thenReturn(usersResource);
        lenient().when(usersResource.get(anyString())).thenReturn(userResource);
        lenient().when(usersResource.list(anyInt(), anyInt())).thenReturn(List.of());
        lenient().when(usersResource.delete(anyString())).thenReturn(mock(Response.class));
    }
}
