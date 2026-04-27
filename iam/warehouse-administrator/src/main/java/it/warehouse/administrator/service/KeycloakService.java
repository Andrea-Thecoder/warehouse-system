package it.warehouse.administrator.service;

import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.role.SimpleRoleTypeDTO;
import it.warehouse.administrator.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
// Valori di default sovrascrivibili da application.properties:
// KeycloakService/CircuitBreaker/requestVolumeThreshold, failureRatio, delay, successThreshold
@CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.6, delay = 10000, successThreshold = 2)
public class KeycloakService {


    @SuppressWarnings({})
    @Inject
    Keycloak keycloak;

    @Inject
    LookupService lookupService;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;
    
    public String createUserAccount(RegisterRequestDTO dto) {
        log.info("createUserAccount: Creating user account on Keycloak");
        RealmResource realmResource = keycloak.realm(realm);
        UserRepresentation user = buildUser(dto);

        try (
                Response response = realmResource.users().create(user)) {
            if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                throw new ServiceException("Username or email already in use");
            }
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                log.error("Keycloak returned status {} during user", response.getStatus());
                throw new ServiceException("Error while creating the user");
            }

            String keycloakUserId = CreatedResponseUtil.getCreatedId(response);
            log.info("User created on Keycloak with id: {}", keycloakUserId);
            return keycloakUserId;
        }
    }

    public void approveUser(String keycloakUserId, List<String> requestedRoles) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            handleKeycloakUser(keycloakUserId, true, realmResource);
            List<RoleRepresentation> roleRepresentations = requestedRoles.stream()
                    .map(roleType -> realmResource.roles().get(roleType).toRepresentation())
                    .toList();
            realmResource.users().get(keycloakUserId).roles().realmLevel().add(roleRepresentations);
            log.info("approveUser: Roles {} assigned to user {}", requestedRoles, keycloakUserId);
        } catch (Exception e) {
            log.error("approveUser: Failed to approve user {} on Keycloak. Error: {}", keycloakUserId, e.getMessage());
            throw new ServiceException("Error while approving the user on Keycloak. Try again later.");
        }
    }

    public void  rejectUser(String keycloakUserId) {
        try (Response ignored = keycloak.realm(realm).users().delete(keycloakUserId)) {
            log.info("rejectUser : User {} deleted from Keycloak (request rejected)", keycloakUserId);
        } catch (Exception e){
            log.error("rejectUser: Error while deleting the user {} . Error message: {}", keycloakUserId, e.getMessage());
            throw new ServiceException("Error while deleting the user. Try again later.");
        }
    }

    public UserRepresentation getUserById(String keycloakUserId) {
        return keycloak.realm(realm).users().get(keycloakUserId).toRepresentation();
    }

    public List<UserRepresentation> fetchUsers(int firstResult, int size) {
        return keycloak.realm(realm).users().list(firstResult, size);
    }

    public int countUsers() {
        return keycloak.realm(realm).users().count();
    }

    public List<SimpleRoleTypeDTO> getRolesFromUser(UserRepresentation user) {
        Map<String, String> rolesMap = lookupService.getRolesMap();
        UserResource userResource = keycloak.realm(realm).users().get(user.getId());
        List<RoleRepresentation> roles = userResource.roles().realmLevel().listAll();
        return roles.stream()
                .filter(role -> rolesMap.containsKey(role.getName()))
                .map(rr -> new SimpleRoleTypeDTO(rr.getName(), rolesMap.get(rr.getName())))
                .collect(Collectors.toList());
    }

    public void changeRolesForUser(String keycloakUserId, Set<String> requestedRoles) {
        log.info("changeRolesForUser: Changing roles for User {} ", keycloakUserId);
        Map<String, String> rolesMap = lookupService.getRolesMap();
        UserResource userResource = keycloak.realm(realm).users().get(keycloakUserId);

        List<RoleRepresentation> currentRoles = userResource.roles().realmLevel().listAll()
                .stream().filter(r -> rolesMap.containsKey(r.getName())).toList();
        userResource.roles().realmLevel().remove(currentRoles);

        List<RoleRepresentation> newRoles = requestedRoles.stream()
                .filter(rolesMap::containsKey)
                .map(r -> keycloak.realm(realm).roles().get(r).toRepresentation())
                .toList();
        userResource.roles().realmLevel().add(newRoles);

        log.info("changeRolesForUser: Roles {} assigned to user {}", requestedRoles, keycloakUserId);
    }


    public void handleEnabledUser(String keycloakUserId,boolean enable) {
        RealmResource realmResource = keycloak.realm(realm);
        handleKeycloakUser(keycloakUserId, enable, realmResource);
        log.info("handleEnabledUser: User {} enabled is updated to keycloak", keycloakUserId);
    }

    public void deleteUser(String keycloakUserId) {
        log.info("deleteUser: Deleting user {} from Keycloak", keycloakUserId);
        rejectUser(keycloakUserId);
        log.info("deleteUser: User {} deleted from Keycloak", keycloakUserId);
    }

    private UserRepresentation buildUser(RegisterRequestDTO dto) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.getPassword());
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEnabled(false);
        user.setCredentials(List.of(credential));
        return user;
    }

    private void handleKeycloakUser(String keycloakUserId, boolean enabled, RealmResource realmResource) {
        UserRepresentation user = realmResource.users().get(keycloakUserId).toRepresentation();
        user.setEnabled(enabled);
        realmResource.users().get(keycloakUserId).update(user);
        log.info("handleKeycloakUser: User {} enabled on Keycloak", keycloakUserId);
    }
}
