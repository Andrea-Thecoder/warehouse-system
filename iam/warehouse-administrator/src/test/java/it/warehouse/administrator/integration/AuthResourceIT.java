package it.warehouse.administrator.integration;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import it.warehouse.administrator.api.AuthResource;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.SimpleResultDTO;
import it.warehouse.administrator.integration.factory.TestDataFactory;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestHTTPEndpoint(AuthResource.class)
@DisplayName("Auth — Registrazione utente")
class AuthResourceIT extends AbstractBaseIT {

    @Test
    @DisplayName("POST /register — dati validi ")
    void register_validRequest_savesRecordAndReturns200() {
        when(keycloakService.createUserAccount(any())).thenReturn(TestDataFactory.FAKE_KC_USER_ID);

        Response response = given()
                .contentType(JSON)
                .body(TestDataFactory.validRegistration())
                .when()
                .post("register")
                .then()
                .extract()
                .response();

        assertEquals(200, response.statusCode());

        SimpleResultDTO<Void> result = response.as(new TypeRef<>() {});
        assertNotNull(result);
        assertNotNull(result.getMessage());

        UserRegistration saved = db.find(UserRegistration.class)
                .where()
                .eq("keycloakUserId", TestDataFactory.FAKE_KC_USER_ID)
                .findOne();

        assertNotNull(saved);
        assertEquals(RegistrationStatus.PENDING, saved.getStatus());
        assertEquals("Mario Rossi", saved.getFullname());
        verify(keycloakService).createUserAccount(any());
    }

    @Test
    @DisplayName("POST /register — body vuoto → 400")
    void register_emptyBody_returns400() {
        given()
                .contentType(JSON)
                .body("{}")
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /register — email non valida → 400")
    void register_invalidEmail_returns400() {
        RegisterRequestDTO dto = TestDataFactory.validRegistration();
        dto.setEmail("not-an-email");

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /register — password troppo corta (< 8 char) → 400")
    void register_shortPassword_returns400() {
        RegisterRequestDTO dto = TestDataFactory.validRegistration();
        dto.setPassword("abc");

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /register — lista ruoli vuota → 400")
    void register_emptyRoleList_returns400() {
        RegisterRequestDTO dto = TestDataFactory.validRegistration();
        dto.setRequestedRoleIds(List.of());

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /register — username mancante → 400")
    void register_missingUsername_returns400() {
        RegisterRequestDTO dto = TestDataFactory.validRegistration();
        dto.setUsername(null);

        given()
                .contentType(JSON)
                .body(dto)
                .when()
                .post("/register")
                .then()
                .statusCode(400);
    }
}