package it.warehouse.administrator.api;

import io.quarkus.security.Authenticated;
import it.warehouse.administrator.dto.LoginRequestDTO;
import it.warehouse.administrator.dto.RefreshTokenRequestDTO;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.SimpleResultDTO;
import it.warehouse.administrator.service.AuthService;
import it.warehouse.administrator.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

    @Inject
    UserService userService;

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
        userService.register(dto);
        return SimpleResultDTO.<Void>builder()
                .message("User created successfully")
                .build();
    }

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Login", description = "Returns access token and refresh token")
    @APIResponse(responseCode = "200", description = "Login successful")
    @APIResponse(responseCode = "401", description = "Invalid credentials or user not yet approved")
    public Response login(@Valid LoginRequestDTO dto) {
        return Response.ok(authService.login(dto)).build();
    }

    @POST
    @Path("/refresh")
    @PermitAll
    @Operation(summary = "Refresh access token using refresh token")
    @APIResponse(responseCode = "200", description = "Token refreshed successfully")
    @APIResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public Response refresh(@Valid RefreshTokenRequestDTO dto) {
        return Response.ok(authService.refresh(dto)).build();
    }

    @POST
    @Path("/logout")
    @Authenticated
    @Operation(summary = "Logout", description = "Invalidates the refresh token on Keycloak")
    @APIResponse(responseCode = "204", description = "Logout successful")
    @APIResponse(responseCode = "401", description = "Not authenticated")
    public Response logout(@Valid RefreshTokenRequestDTO dto) {
        authService.logout(dto);
        return Response.noContent().build();
    }
}