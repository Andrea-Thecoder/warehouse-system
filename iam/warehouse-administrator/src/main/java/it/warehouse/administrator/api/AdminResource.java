package it.warehouse.administrator.api;


import it.warehouse.administrator.dto.PagedResultDTO;
import it.warehouse.administrator.dto.SimpleResultDTO;
import it.warehouse.administrator.dto.admin.RolesInDTO;
import it.warehouse.administrator.dto.search.BaseSearchRequest;
import it.warehouse.administrator.dto.user.SimpleKeycloakUserDTO;
import it.warehouse.administrator.dto.user.SimpleUserRegistrationDTO;
import it.warehouse.administrator.service.AdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/admin-managment")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Admin Managment", description = "Admin endpoint for User management")
@RolesAllowed({"ADMIN"})
public class AdminResource {

    @Inject
    AdminService adminService;


    @GET
    @Path("/users")
    @Operation(summary = "List Keycloak users", description = "Paginated list of all realm users with their roles")
    @APIResponse(responseCode = "200", description = "List of users")
    public PagedResultDTO<SimpleKeycloakUserDTO> listUsers(
            @BeanParam BaseSearchRequest request
    ) {
        return adminService.findKeycloakUser(request);
    }

    @GET
    @Path("user-pending")
    @Operation(summary = "List user requests", description = "Filterable by status: PENDING, APPROVED, REJECTED")
    @APIResponse(responseCode = "200", description = "List of user requests")
    public PagedResultDTO<SimpleUserRegistrationDTO> list(@BeanParam BaseSearchRequest request) {
        return adminService.findUserRegistration(request);
    }

    @POST
    @Path("/{id}/approve")
    @Operation(
            summary = "Approve request",
            description = "Enables the user on Keycloak and assigns the requested roles"
    )
    @APIResponse(responseCode = "204", description = "Request approved successfully")
    @APIResponse(responseCode = "400", description = "Request is not in PENDING status")
    @APIResponse(responseCode = "404", description = "Request not found")
    public SimpleResultDTO<Void> approve(
            @PathParam("id") UUID id,
            @Valid RolesInDTO dto
    ) {
        adminService.handleApprove(id, dto.getApprovedRoles());
        return SimpleResultDTO.<Void>builder()
                .message("User approved successfully")
                .build();
    }

    @POST
    @Path("/{id}/reject")
    @Operation(
            summary = "Reject request",
            description = "Deletes the user from Keycloak and marks the request as REJECTED"
    )
    @APIResponse(responseCode = "204", description = "Request rejected successfully")
    @APIResponse(responseCode = "400", description = "Request is not in PENDING status")
    @APIResponse(responseCode = "404", description = "Request not found")
    public SimpleResultDTO<Void> reject(@PathParam("id") UUID id) {
        adminService.handleReject(id);
        return SimpleResultDTO.<Void>builder()
                .message("User rejected successfully")
                .build();
    }

    @PATCH
    @Path("/users/{userId}/manager-enable")
    @Operation(summary = "manager enabled status", description = "manager enable status for the user on Keycloak")
    @APIResponse(responseCode = "200", description = "User disabled successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    public SimpleResultDTO<Void> handleEnabledUser(
            @PathParam("userId") String userId,
            @QueryParam("enable") boolean enable) {
        adminService.handleEnabledUser(userId,enable);
        return SimpleResultDTO.<Void>builder()
                .message("User disabled successfully")
                .build();
    }

    @DELETE
    @Path("/users/{userId}")
    @Operation(summary = "Delete user", description = "Permanently deletes the user from Keycloak")
    @APIResponse(responseCode = "200", description = "User deleted successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    public SimpleResultDTO<Void> deleteUser(@PathParam("userId") String userId) {
        adminService.deleteUser(userId);
        return SimpleResultDTO.<Void>builder()
                .message("User deleted successfully")
                .build();
    }

    @PUT
    @Path("/users/{userId}/roles")
    @Operation(summary = "Change user roles", description = "Replaces the current realm roles of a user")
    @APIResponse(responseCode = "200", description = "Roles updated successfully")
    @APIResponse(responseCode = "404", description = "User not found")
    public SimpleResultDTO<Void> changeRole(
            @PathParam("userId") String userId,
            @Valid RolesInDTO dto
    ) {
        adminService.changeRole(userId, dto.getApprovedRoles());
        return SimpleResultDTO.<Void>builder()
                .message("Roles updated successfully")
                .build();
    }

}
