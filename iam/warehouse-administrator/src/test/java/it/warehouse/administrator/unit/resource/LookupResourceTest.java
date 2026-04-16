package it.warehouse.administrator.unit.resource;

import it.warehouse.administrator.api.LookupResource;
import it.warehouse.administrator.model.RoleType;
import it.warehouse.administrator.service.LookupService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LookupResourceTest {

    @Mock
    LookupService lookupService;

    @InjectMocks
    LookupResource  lookupResource;

    @Test
    public void getRolesTest(){
        when(lookupService.findRoles()).thenReturn(List.of(new RoleType("admin","admin","admin")));
        var response =  lookupResource.getRoles();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertInstanceOf(List.class, response.getEntity());
        verify(lookupService, times(1)).findRoles();
    }
}
