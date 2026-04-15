package it.warehouse.administrator.service;

import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.Query;
import io.ebean.Transaction;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.exception.ServiceException;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private Database db;
    @Mock
    private KeycloakService keycloakService;
    @Mock
    private LookupService lookupService;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    // -------------------------------------------------------------------------
    // register
    // -------------------------------------------------------------------------

    @Test
    void register_success_insertsEntityAndSetsKeycloakId() {
        RegisterRequestDTO dto = buildRegisterDto();
        Transaction tx = mock(Transaction.class);
        when(db.beginTransaction()).thenReturn(tx);
        when(lookupService.getRoleTypesForRegistration(dto.getRequestedRoleIds())).thenReturn(List.of());
        when(keycloakService.createUserAccount(dto)).thenReturn("kc-user-id");

        try (MockedConstruction<UserRegistration> mocked = mockConstruction(UserRegistration.class)) {
            userRegistrationService.registerUser(dto);

            assertThat(mocked.constructed()).hasSize(1);
            UserRegistration created = mocked.constructed().getFirst();
            verify(created).setFullname("Mario Rossi");
            verify(created).setStatus(RegistrationStatus.PENDING);
            verify(created).insert(tx);
            verify(created).setKeycloakUserId("kc-user-id");
            verify(created).update(tx);
            verify(tx).commit();
        }
    }

    @Test
    void register_keycloakReturnsBlankId_throwsServiceException() {
        RegisterRequestDTO dto = buildRegisterDto();
        Transaction tx = mock(Transaction.class);
        when(db.beginTransaction()).thenReturn(tx);
        when(lookupService.getRoleTypesForRegistration(any())).thenReturn(List.of());
        when(keycloakService.createUserAccount(dto)).thenReturn("  "); // blank

        try (MockedConstruction<UserRegistration> ignored = mockConstruction(UserRegistration.class)) {
            assertThatThrownBy(() -> userRegistrationService.registerUser(dto))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("Error while creating the user");
        }
    }

    @Test
    void register_keycloakThrowsException_throwsServiceException() {
        RegisterRequestDTO dto = buildRegisterDto();
        Transaction tx = mock(Transaction.class);
        when(db.beginTransaction()).thenReturn(tx);
        when(lookupService.getRoleTypesForRegistration(any())).thenReturn(List.of());
        when(keycloakService.createUserAccount(dto)).thenThrow(new RuntimeException("Keycloak down"));

        try (MockedConstruction<UserRegistration> ignored = mockConstruction(UserRegistration.class)) {
            assertThatThrownBy(() -> userRegistrationService.registerUser(dto))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Test
    void updateRegistrationRequest_setsStatusAndCommitsTransaction() {
        UserRegistration reg = mock(UserRegistration.class);
        Transaction tx = mock(Transaction.class);
        when(db.beginTransaction()).thenReturn(tx);

        userRegistrationService.updateRegistrationRequest(reg, RegistrationStatus.APPROVED);

        verify(reg).setStatus(RegistrationStatus.APPROVED);
        verify(reg).update(tx);
        verify(tx).commit();
    }

    @Test
    void updateRegistrationRequest_onDbException_throwsServiceException() {
        UserRegistration reg = mock(UserRegistration.class);
        Transaction tx = mock(Transaction.class);
        when(db.beginTransaction()).thenReturn(tx);
        doThrow(new RuntimeException("DB unavailable")).when(reg).update(tx);

        assertThatThrownBy(() -> userRegistrationService.updateRegistrationRequest(reg, RegistrationStatus.REJECTED))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Error while updating the user");
    }

    @Test
    void getRegistrationPendingOrThrow_found_returnsEntity() {
        UUID id = UUID.randomUUID();
        UserRegistration expected = mock(UserRegistration.class);
        stubQueryChain(id, Optional.of(expected));

        UserRegistration result = userRegistrationService.getRegistrationPendingOrThrow(id);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getRegistrationPendingOrThrow_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        stubQueryChain(id, Optional.empty());

        assertThatThrownBy(() -> userRegistrationService.getRegistrationPendingOrThrow(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @SuppressWarnings("unchecked")
    private void stubQueryChain(UUID id, Optional<UserRegistration> result) {
        Query<UserRegistration> query = mock(Query.class);
        ExpressionList<UserRegistration> exl = mock(ExpressionList.class);
        when(db.find(UserRegistration.class)).thenReturn(query);
        when(query.where()).thenReturn(exl);
        when(exl.idEq(id)).thenReturn(exl);
        when(exl.eq(anyString(), any())).thenReturn(exl);
        when(exl.isNotNull(anyString())).thenReturn(exl);
        when(exl.findOneOrEmpty()).thenReturn(result);
    }

    private RegisterRequestDTO buildRegisterDto() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("mario.rossi");
        dto.setEmail("mario@example.com");
        dto.setFirstName("Mario");
        dto.setLastName("Rossi");
        dto.setPassword("password123");
        dto.setRequestedRoleIds(List.of("ADMIN"));
        return dto;
    }
}