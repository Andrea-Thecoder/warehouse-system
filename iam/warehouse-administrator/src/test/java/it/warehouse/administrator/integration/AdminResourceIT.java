package it.warehouse.administrator.integration;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import it.warehouse.administrator.api.AdminResource;
import it.warehouse.administrator.dto.PagedResultDTO;
import it.warehouse.administrator.dto.SimpleResultDTO;
import it.warehouse.administrator.dto.admin.RolesInDTO;
import it.warehouse.administrator.dto.user.SimpleKeycloakUserDTO;
import it.warehouse.administrator.dto.user.SimpleUserRegistrationDTO;
import it.warehouse.administrator.integration.factory.TestDataFactory;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

@QuarkusTest
@TestHTTPEndpoint(AdminResource.class)
@TestSecurity(user = "admin", roles = {"ADMIN"})
@DisplayName("Admin — Gestione utenti")
class AdminResourceIT extends AbstractBaseIT {

    @Test
    @DisplayName("GET /users")
    void listUsers_withUsers_returns200() {
        mockFetchUsersOk(List.of(TestDataFactory.keycloakUser()));
        mockGetRolesFromUserOk(List.of(TestDataFactory.warehouseOperatorRole()));

        Response response = given()
                .queryParam("page", 1)
                .queryParam("size", 20)
                .when()
                .get("/users")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        PagedResultDTO<SimpleKeycloakUserDTO> result = response.as(new TypeRef<>() {});
        assertNotNull(result);
        assertFalse(result.getList().isEmpty());
        assertEquals(TestDataFactory.FAKE_KC_USER_ID, result.getList().getFirst().getId());

        verify(keycloakService).fetchUsers(anyInt(), anyInt());
        verify(keycloakService).getRolesFromUser(any());
    }

    @Test
    @DisplayName("GET /users")
    void listUsers_keycloakError_returns400() {
        mockFetchUsersThrows();

        given()
                .queryParam("page", 1)
                .queryParam("size", 20)
                .when()
                .get("/users")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /user-pending ")
    void listPending_withRecords_returns200() {
        TestDataFactory.pendingRegistration(db, TestDataFactory.FAKE_KC_USER_ID);

        Response response = given()
                .queryParam("page", 1)
                .queryParam("size", 20)
                .when()
                .get("/user-pending")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        PagedResultDTO<SimpleUserRegistrationDTO> result = response.as(new TypeRef<>() {});
        assertNotNull(result);
        assertFalse(result.getList().isEmpty());
    }

    @Test
    @DisplayName("GET /user-pending ")
    void listPending_noRecords_returns200EmptyList() {
        Response response = given()
                .queryParam("page", 1)
                .queryParam("size", 20)
                .when()
                .get("/user-pending")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        PagedResultDTO<SimpleUserRegistrationDTO> result = response.as(new TypeRef<>() {});
        assertNotNull(result);
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("POST /{id}/approve ")
    void approve_allRoles_statusApproved() {
        UserRegistration reg = TestDataFactory.pendingRegistration(db, TestDataFactory.FAKE_KC_USER_ID);
        mockApproveUserOk();

        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("WAREHOUSE_OPERATOR"));

        Response response = given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/" + reg.getId() + "/approve")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        SimpleResultDTO<Void> result = response.as(new TypeRef<>() {});
        assertNotNull(result.getMessage());

        UserRegistration updated = db.find(UserRegistration.class, reg.getId());
        assertEquals(RegistrationStatus.APPROVED, updated.getStatus());

        verify(keycloakService).approveUser(any(), any());
    }

    @Test
    @DisplayName("POST /{id}/approve")
    void approve_partialRoles_statusPartialApproved() {
        UserRegistration reg = TestDataFactory.pendingRegistrationMultiRole(db, TestDataFactory.FAKE_KC_USER_ID);
        mockApproveUserOk();

        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("WAREHOUSE_OPERATOR"));

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/" + reg.getId() + "/approve")
                .then()
                .statusCode(200);

        UserRegistration updated = db.find(UserRegistration.class, reg.getId());
        assertNotNull(updated);
        assertEquals(RegistrationStatus.PARTIAL_APPROVED, updated.getStatus());
    }

    @Test
    @DisplayName("POST /{id}/approve ")
    void approve_emptyRoles_returns400() {
        given()
                .contentType(JSON)
                .body("{}")
                .when()
                .post("/" + UUID.randomUUID() + "/approve")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /{id}/approve ")
    void approve_notFound_returns404() {
        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("WAREHOUSE_OPERATOR"));

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/" + UUID.randomUUID() + "/approve")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /{id}/approve ")
    void approve_keycloakError_dbUpdatedAnd400() {
        UserRegistration reg = TestDataFactory.pendingRegistration(db, TestDataFactory.FAKE_KC_USER_ID);
        mockApproveUserThrows();

        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("WAREHOUSE_OPERATOR"));

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/" + reg.getId() + "/approve")
                .then()
                .statusCode(400);

        UserRegistration updated = db.find(UserRegistration.class, reg.getId());
        assertNotNull(updated);
        assertEquals(RegistrationStatus.APPROVED, updated.getStatus());
    }

    @Test
    @DisplayName("POST /{id}/reject ")
    void reject_pending_statusRejected() {
        UserRegistration reg = TestDataFactory.pendingRegistration(db, TestDataFactory.FAKE_KC_USER_ID);
        mockRejectUserOk();
        String url = "/" + reg.getId() + "/reject";

        Response response = given()
                .contentType(JSON)
                .when()
                .post(url)
                .then()
                .extract().response();
        assertEquals(200, response.statusCode());

        SimpleResultDTO<Void> result = response.as(new TypeRef<>() {});
        assertNotNull(result.getMessage());

        UserRegistration updated = db.find(UserRegistration.class, reg.getId());
        assertNotNull(updated);
        assertEquals(RegistrationStatus.REJECTED, updated.getStatus());

        verify(keycloakService).rejectUser(TestDataFactory.FAKE_KC_USER_ID);
    }

    @Test
    @DisplayName("POST /{id}/reject ")
    void reject_notFound_returns404() {
        given()
                .contentType(JSON)
                .when()
                .post("/" + UUID.randomUUID() + "/reject")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /{id}/reject ")
    void reject_keycloakError_dbUpdatedAnd400() {
        UserRegistration reg = TestDataFactory.pendingRegistration(db, TestDataFactory.FAKE_KC_USER_ID);
        mockRejectUserThrows();

        given()
                .contentType(JSON)
                .when()
                .post("/" + reg.getId() + "/reject")
                .then()
                .statusCode(400);

        UserRegistration updated = db.find(UserRegistration.class, reg.getId());
        assertEquals(RegistrationStatus.REJECTED, updated.getStatus());
    }

    @Test
    @DisplayName("PATCH /users/{id}/manager-enable?enable=true")
    void handleEnabledUser_enable_returns200() {
        mockHandleEnabledUserOk();

        Response response = given()
                .contentType(JSON)
                .queryParam("enable", true)
                .when()
                .patch("/users/" + TestDataFactory.FAKE_KC_USER_ID + "/manager-enable")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());
        verify(keycloakService).handleEnabledUser(TestDataFactory.FAKE_KC_USER_ID, true);
    }

    @Test
    @DisplayName("PATCH /users/{id}/manager-enable?enable=false ")
    void handleEnabledUser_disable_returns200() {
        mockHandleEnabledUserOk();

        given()
                .contentType(JSON)
                .queryParam("enable", false)
                .when()
                .patch("/users/" + TestDataFactory.FAKE_KC_USER_ID + "/manager-enable")
                .then()
                .statusCode(200);

        verify(keycloakService).handleEnabledUser(TestDataFactory.FAKE_KC_USER_ID, false);
    }

    @Test
    @DisplayName("PATCH /users/{id}/manager-enable ")
    void handleEnabledUser_keycloakError_returns400() {
        mockHandleEnabledUserThrows();

        given()
                .contentType(JSON)
                .queryParam("enable", true)
                .when()
                .patch("/users/" + TestDataFactory.FAKE_KC_USER_ID + "/manager-enable")
                .then()
                .statusCode(400);
    }


    @Test
    @DisplayName("DELETE /users/{id}")
    void deleteUser_exists_returns200() {
        mockDeleteUserOk();

        Response response = given()
                .contentType(JSON)
                .when()
                .delete("/users/" + TestDataFactory.FAKE_KC_USER_ID)
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        SimpleResultDTO<Void> result = response.as(new TypeRef<>() {});
        assertNotNull(result.getMessage());

        verify(keycloakService).deleteUser(TestDataFactory.FAKE_KC_USER_ID);
    }

    @Test
    @DisplayName("DELETE /users/{id")
    void deleteUser_keycloakError_returns400() {
        mockDeleteUserThrows();

        given()
                .contentType(JSON)
                .when()
                .delete("/users/" + TestDataFactory.FAKE_KC_USER_ID)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("PUT /users/{id}/roles — cambio ruoli ")
    void changeRole_validRoles_returns200() {
        mockChangeRolesForUserOk();

        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("WAREHOUSE_OPERATOR"));

        Response response = given()
                .contentType(JSON)
                .body(dto)
                .when()
                .put("/users/" + TestDataFactory.FAKE_KC_USER_ID + "/roles")
                .then()
                .extract().response();

        assertEquals(200, response.statusCode());

        SimpleResultDTO<Void> result = response.as(new TypeRef<>() {});
        assertNotNull(result.getMessage());

        verify(keycloakService).changeRolesForUser(any(), any());
    }

    @Test
    @DisplayName("PUT /users/{id}/roles — lista ruoli vuota → 400")
    void changeRole_emptyRoles_returns400() {
        given()
                .contentType(JSON)
                .body("{}")
                .when()
                .put("/users/" + TestDataFactory.FAKE_KC_USER_ID + "/roles")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("PUT /users/{id}/roles — errore keycloak → 400")
    void changeRole_keycloakError_returns400() {
        mockChangeRolesForUserThrows();

        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("WAREHOUSE_OPERATOR"));

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .put("/users/" + TestDataFactory.FAKE_KC_USER_ID + "/roles")
                .then()
                .statusCode(400);
    }

    // -------------------------------------------------------------------------
    // Security — accesso non autorizzato
    // -------------------------------------------------------------------------

    @Test
    @TestSecurity(user = "unauthorized", roles = {"USER"})
    @DisplayName("Qualsiasi endpoint — ruolo non ADMIN → 403")
    void anyEndpoint_withoutAdminRole_returns403() {
        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("WAREHOUSE_OPERATOR"));

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/" + UUID.randomUUID() + "/approve")
                .then()
                .statusCode(403);
    }
}