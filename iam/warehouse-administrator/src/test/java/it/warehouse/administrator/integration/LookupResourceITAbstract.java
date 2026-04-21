package it.warehouse.administrator.integration;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import it.warehouse.administrator.api.LookupResource;
import it.warehouse.administrator.model.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("LookupResourceITAbstract")
@TestHTTPEndpoint(LookupResource.class)
class LookupResourceITAbstract extends AbstractBaseIT {

    @Test
    @DisplayName("GET /lookup/roles ")
    void getRoles_returns5RolesOrderedByLabel() {
        Response response = given()
                .when()
                .get("/roles")
                .then()
                .extract()
                .response();

        assertEquals(200, response.statusCode());

        List<RoleType> result = response.as(new TypeRef<>() {});
        assertNotNull(result);
        assertNotNull(result.getFirst());
        assertFalse(result.isEmpty());
    }
}