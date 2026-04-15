package it.warehouse.administrator.api;

import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.SimpleResultDTO;
import it.warehouse.administrator.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Registration, login and token management")
public class AuthResource {

    @Inject
    AuthService authService;
    @POST
    @Path("/register")
    @PermitAll
    @Operation(
            summary = "Registration request",
            description = "Creates a disabled user on Keycloak and saves the request in PENDING status. " +
                          "An administrator must approve it before the user can log in."
    )
    @APIResponse(responseCode = "201", description = "Registration request created successfully")
    @APIResponse(responseCode = "400", description = "Invalid data or username/email already in use")
    public SimpleResultDTO<Void> register(@Valid RegisterRequestDTO dto) {
        authService.register(dto);
        return SimpleResultDTO.<Void>builder()
                .message("User created successfully")
                .build();
    }

}