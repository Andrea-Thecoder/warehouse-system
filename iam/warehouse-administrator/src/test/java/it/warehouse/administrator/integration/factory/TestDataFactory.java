package it.warehouse.administrator.integration.factory;

import io.ebean.Database;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.role.SimpleRoleTypeDTO;
import it.warehouse.administrator.model.RoleType;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import org.keycloak.representations.idm.UserRepresentation;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static final String FAKE_KC_USER_ID = UUID.randomUUID().toString();

    public static UserRepresentation keycloakUser() {
        UserRepresentation u = new UserRepresentation();
        u.setId(FAKE_KC_USER_ID);
        u.setUsername("mario.rossi");
        u.setFirstName("Mario");
        u.setLastName("Rossi");
        u.setEnabled(true);
        return u;
    }

    public static SimpleRoleTypeDTO warehouseOperatorRole() {
        return new SimpleRoleTypeDTO("WAREHOUSE_OPERATOR", "Operatore Magazzino");
    }

    public static UserRegistration expiredPendingRegistration(Database db, String keycloakUserId) {
        UserRegistration reg = pendingRegistration(db, keycloakUserId);
        db.sqlUpdate("UPDATE user_registration SET _data_creazione = :ts WHERE id = :id")
                .setParameter("ts", Timestamp.valueOf(LocalDateTime.now().minusDays(30)))
                .setParameter("id", reg.getId())
                .execute();
        return reg;
    }

    public static RegisterRequestDTO validRegistration() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("mario.rossi");
        dto.setEmail("mario.rossi@warehouse.it");
        dto.setFirstName("Mario");
        dto.setLastName("Rossi");
        dto.setPassword("Password123!");
        dto.setRequestedRoleIds(List.of("WAREHOUSE_OPERATOR"));
        return dto;
    }

    public static UserRegistration pendingRegistration(Database db, String keycloakUserId) {
        RoleType role = db.find(RoleType.class).setId("WAREHOUSE_OPERATOR").findOne();
        UserRegistration reg = new UserRegistration();
        reg.setFullname("Mario Rossi");
        reg.setKeycloakUserId(keycloakUserId);
        reg.setStatus(RegistrationStatus.PENDING);
        reg.setRequestedRoleType(List.of(role));
        reg.save();
        return reg;
    }

    public static UserRegistration pendingRegistrationMultiRole(Database db, String keycloakUserId) {
        List<RoleType> roles = db.find(RoleType.class)
                .where()
                .idIn(List.of("WAREHOUSE_OPERATOR", "WAREHOUSE_ADMIN"))
                .findList();
        UserRegistration reg = new UserRegistration();
        reg.setFullname("Luigi Bianchi");
        reg.setKeycloakUserId(keycloakUserId);
        reg.setStatus(RegistrationStatus.PENDING);
        reg.setRequestedRoleType(roles);
        reg.save();
        return reg;
    }
}