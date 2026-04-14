package it.warehouse.administrator.api;

import it.warehouse.administrator.service.LookupService;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/lookup")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "LookupService", description = "LookupService lists: Roles")
public class Lookup {

    @Inject
    LookupService  lookupService;


    @GET
    @Path("roles")
    @PermitAll
    @Operation(summary = "List available roles", description = "Returns the roles a user can request during user")
    @APIResponse(responseCode = "200", description = "List of available roles")
    public Response getRoles() {
        return Response.ok(lookupService.findRoles()).build();
    }

}
