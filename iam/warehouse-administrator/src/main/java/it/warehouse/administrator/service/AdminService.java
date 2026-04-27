package it.warehouse.administrator.service;

import io.ebean.Database;
import it.warehouse.administrator.dto.PagedResultDTO;
import it.warehouse.administrator.dto.search.BaseSearchRequest;
import it.warehouse.administrator.dto.user.SimpleKeycloakUserDTO;
import it.warehouse.administrator.dto.user.SimpleUserRegistrationDTO;
import it.warehouse.administrator.model.RoleType;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import it.warehouse.administrator.security.JwtService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class AdminService {

    @Inject
    KeycloakService keycloakService;

    @Inject
    UserRegistrationService userRegistrationService;

    @Inject
    Database db;


    public PagedResultDTO<SimpleUserRegistrationDTO> findUserRegistration(BaseSearchRequest request) {
        return userRegistrationService.findRegistrationRequest(request);
    }

    /**
     * Lists all Keycloak realm users with their roles from the DB RoleType table.
     * Total calls: 1 Keycloak (users list) + 1 Keycloak (count) + N Keycloak (roles per user, one per page item).
     */
    public PagedResultDTO<SimpleKeycloakUserDTO> findKeycloakUser(BaseSearchRequest request) {
        log.info("findKeycloakUser: Starting retrieving user from keycloak.");
        int page = request.getPage();
        int size = request.getSize();
        int firstResult = (page - 1) * size;

        List<UserRepresentation> keycloakUsers = keycloakService.fetchUsers(firstResult, size);
        int total = keycloakService.countUsers();
        List<SimpleKeycloakUserDTO> dtos = keycloakUsers.stream()
                .map(u -> SimpleKeycloakUserDTO.of(u, keycloakService.getRolesFromUser(u)))
                .toList();

        return PagedResultDTO.of(dtos, total, firstResult, size);
    }

    /**
     * Approves the request: updates the DB status first, then enables the user on Keycloak
     * and assigns roles. DB is updated first to avoid inconsistency — if Keycloak fails
     * after the commit, the DB record can be used for manual retry.
     */
    public void handleApprove(UUID registrationId, Set<String> approvedRoles) {
        log.info("handleApprove: approve registrationId={}, approvedRoles={}", registrationId, approvedRoles);
        UserRegistration userRegistration = userRegistrationService.getRegistrationPendingOrThrow(registrationId);
        Set<String> requestedRoles = userRegistration.getRequestedRoleType().stream().map(RoleType::getId).collect(Collectors.toSet());
        List<String> validRoles = approvedRoles.stream().filter(requestedRoles::contains).toList();
        RegistrationStatus status = validRoles.size() == requestedRoles.size()
                ? RegistrationStatus.APPROVED
                : RegistrationStatus.PARTIAL_APPROVED;

        userRegistrationService.updateRegistrationRequest(userRegistration, status);
        keycloakService.approveUser(userRegistration.getKeycloakUserId(), validRoles);


        log.info("handleApprove: Request {} approved with status {}", registrationId, status);
    }

    /**
     * Rejects the request: updates the DB status first, then deletes the user from Keycloak.
     */
    public void handleReject(UUID registrationId) {
        log.info("rejectUser: reject user with registrationId={}", registrationId);
        UserRegistration userRegistration = userRegistrationService.getRegistrationPendingOrThrow(registrationId);

        userRegistrationService.updateRegistrationRequest(userRegistration, RegistrationStatus.REJECTED);
        keycloakService.rejectUser(userRegistration.getKeycloakUserId());

        log.info("reject: Request {} rejected.", registrationId);
    }

    public void changeRole(String userId, Set<String> requestedRoles) {
        log.info("changeRole: Changing roles for User {} ", userId);
        keycloakService.changeRolesForUser(userId, requestedRoles);
    }

    public void handleEnabledUser(String userId, boolean enable) {
        log.info("handleEnabledUser: disable user with id={}", userId);
        keycloakService.handleEnabledUser(userId,enable);
    }

    public void deleteUser(String userId) {
        log.info("deleteUser: delete user with id={}", userId);
        keycloakService.deleteUser(userId);
    }


}
