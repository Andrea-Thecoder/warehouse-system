package it.warehouse.administrator.service;

import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Transaction;
import it.warehouse.administrator.dto.PagedResultDTO;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.search.BaseSearchRequest;
import it.warehouse.administrator.dto.user.SimpleUserRegistrationDTO;
import it.warehouse.administrator.exception.ServiceException;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@ApplicationScoped
@Slf4j
public class UserRegistrationService {

    @Inject
    Database db;

    @Inject
    KeycloakService keycloakService;

    @Inject
    LookupService  lookupService;

    /**
     * Creates the user on Keycloak in disabled state and saves the user
     * request in the DB with PENDING status.
     * Sensitive data (name, email, password) lives exclusively on Keycloak.
     */
    public void registerUser(RegisterRequestDTO dto) {
        log.info("registerUser: New user request for username: {}", dto.getUsername());

        try (Transaction tx = db.beginTransaction()) {
            UserRegistration rr = toUserRegistrationEntity(dto);
            rr.insert(tx);
            String userId = keycloakService.createUserAccount(dto);
            if(StringUtils.isBlank(userId)){
                log.error("registerUser: Error while creating new user");
                throw new ServiceException("Error while creating the user. Try again later.");
            }
            rr.setKeycloakUserId(userId);
            rr.update(tx);
            tx.commit();
        }catch (Exception e) {
            log.error("register: Error while creating the user. Error message: {}", e.getMessage());
            throw new ServiceException("Error while creating the user. Try again later.");
        }
    }

    /**
     * Lists all requests filtered by status (null = all).
     */
    public PagedResultDTO<SimpleUserRegistrationDTO> findRegistrationRequest(BaseSearchRequest request) {
        ExpressionList<UserRegistration> exl = db.find(UserRegistration.class)
                .setLabel("findRegistrationRequest")
                .where()
                .eq("status",RegistrationStatus.PENDING);

        request.pagination(exl,"_dataCreazione desc");
        PagedList<UserRegistration> registrationPagedList = exl.findPagedList();
        return PagedResultDTO.of(registrationPagedList, SimpleUserRegistrationDTO::of);
    }

    public void updateRegistrationRequest(UserRegistration userRegistration, RegistrationStatus status){
        try(Transaction tx = db.beginTransaction()) {
            userRegistration.setStatus(status);
            userRegistration.update(tx);
            tx.commit();
        } catch (Exception e) {
            log.error("updateRegistrationRequest: Error while updating the user. Error message: {}", e.getMessage());
            throw new ServiceException("Error while updating the user. Try again later.");
        }
    }


    public UserRegistration toUserRegistrationEntity(RegisterRequestDTO dto){
        UserRegistration request = new UserRegistration();
        request.setFullname(dto.getFirstName()+" "+dto.getLastName());
        request.setRequestedRoleType(lookupService.getRoleTypesForRegistration(dto.getRequestedRoleIds()));
        request.setStatus(RegistrationStatus.PENDING);
        return request;
    }


    public UserRegistration getRegistrationPendingOrThrow(UUID id) {
        return db.find(UserRegistration.class)
                .setLabel("getRegistrationPendingOrThrow")
                .where()
                .idEq(id)
                .eq("status", RegistrationStatus.PENDING)
                .isNotNull("keycloakUserId")
                .findOneOrEmpty()
                .orElseThrow(() -> {
                    log.error("findRegistrationOrThrow: Registration request not found: {}", id);
                    return new NotFoundException("findRegistrationOrThrow: Registration request not found: " + id);
                });
    }
}