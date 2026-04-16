package it.warehouse.administrator.unit.resource;



import io.quarkus.test.security.TestSecurity;
import it.warehouse.administrator.api.AdminResource;
import it.warehouse.administrator.dto.PagedResultDTO;
import it.warehouse.administrator.dto.SimpleResultDTO;
import it.warehouse.administrator.dto.admin.RolesInDTO;
import it.warehouse.administrator.dto.search.BaseSearchRequest;
import it.warehouse.administrator.dto.user.SimpleKeycloakUserDTO;
import it.warehouse.administrator.dto.user.SimpleUserRegistrationDTO;
import it.warehouse.administrator.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminResourceTest {

    @Mock
    AdminService adminService;

    @InjectMocks
    AdminResource adminResource;

    @Test
    void listUsers() {
        BaseSearchRequest request = new BaseSearchRequest();

        PagedResultDTO<SimpleKeycloakUserDTO> mockResult = mock(PagedResultDTO.class);

        when(adminService.findKeycloakUser(request)).thenReturn(mockResult);

        PagedResultDTO<SimpleKeycloakUserDTO> response =
                adminResource.listUsers(request);

        assertNotNull(response);
        assertNotNull(response.getList());
        assertEquals(0L, response.getTotalRows());

        verify(adminService).findKeycloakUser(request);
    }

    @Test
    void listUserPending() {
        BaseSearchRequest request = new BaseSearchRequest();

        PagedResultDTO<SimpleUserRegistrationDTO> mockResult = mock(PagedResultDTO.class);

        when(adminService.findUserRegistration(request)).thenReturn(mockResult);

        PagedResultDTO<SimpleUserRegistrationDTO> response =
                adminResource.list(request);

        assertNotNull(response);
        assertNotNull(response.getList());
        assertEquals(0L, response.getTotalRows());

        verify(adminService).findUserRegistration(request);
    }

    @Test
    void approveUser() {
        UUID id = UUID.randomUUID();

        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("ADMIN"));

        doNothing().when(adminService).handleApprove(id, dto.getApprovedRoles());

        SimpleResultDTO<Void> response = adminResource.approve(id, dto);

        assertNotNull(response);
        assertNotNull(response.getMessage());

        verify(adminService).handleApprove(id, dto.getApprovedRoles());
    }

    @Test
    void rejectUser() {
        UUID id = UUID.randomUUID();

        doNothing().when(adminService).handleReject(id);

        SimpleResultDTO<Void> response = adminResource.reject(id);

        assertNotNull(response);
        assertNotNull(response.getMessage());

        verify(adminService).handleReject(id);
    }

    @Test
    void enableUser() {
        String userId = "123";
        boolean enable = true;

        doNothing().when(adminService).handleEnabledUser(userId, enable);

        SimpleResultDTO<Void> response =
                adminResource.handleEnabledUser(userId, enable);

        assertNotNull(response);
        assertNotNull(response.getMessage());

        verify(adminService).handleEnabledUser(userId, enable);
    }

    @Test
    void deleteUser() {
        String userId = "123";

        doNothing().when(adminService).deleteUser(userId);

        SimpleResultDTO<Void> response = adminResource.deleteUser(userId);

        assertNotNull(response);
        assertNotNull(response.getMessage());

        verify(adminService).deleteUser(userId);
    }

    @Test
    void changeRole() {
        String userId = "123";

        RolesInDTO dto = new RolesInDTO();
        dto.setApprovedRoles(Set.of("ADMIN"));

        doNothing().when(adminService)
                .changeRole(userId, dto.getApprovedRoles());

        SimpleResultDTO<Void> response =
                adminResource.changeRole(userId, dto);

        assertNotNull(response);
        assertNotNull(response.getMessage());

        verify(adminService)
                .changeRole(userId, dto.getApprovedRoles());
    }

}
