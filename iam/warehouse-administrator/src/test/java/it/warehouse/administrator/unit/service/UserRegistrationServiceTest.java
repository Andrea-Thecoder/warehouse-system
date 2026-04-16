package it.warehouse.administrator.unit.service;


import io.ebean.*;
import it.warehouse.administrator.dto.RegisterRequestDTO;
import it.warehouse.administrator.dto.search.BaseSearchRequest;
import it.warehouse.administrator.exception.ServiceException;
import it.warehouse.administrator.model.RoleType;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import it.warehouse.administrator.service.KeycloakService;
import it.warehouse.administrator.service.LookupService;
import it.warehouse.administrator.service.UserRegistrationService;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @InjectMocks
    @Spy
    UserRegistrationService userRegistrationService;

    @Mock
    KeycloakService keycloakService;

    @Mock
    LookupService lookupService;

    @Mock
    Database db;

    @Mock
    Transaction transaction;

    @Mock
    Query<UserRegistration> query;

    @Mock
    ExpressionList<UserRegistration> expressionList;

    @Mock
    PagedList<UserRegistration> pagedList;

    @BeforeEach
    void setUp() {
        lenient().when(db.beginTransaction()).thenReturn(transaction);
    }

    @Test
    @Order(1)
    void findUserTest() {
        BaseSearchRequest request = mock(BaseSearchRequest.class);
        baseQuery();
        when(expressionList.eq(anyString(), any(RegistrationStatus.class))).thenReturn(expressionList);
        when(expressionList.findPagedList()).thenReturn(pagedList);

        var result = userRegistrationService.findRegistrationRequest(request);

        assertNotNull(result);
        assertNotNull(result.getList());
        verify(request).pagination(any(ExpressionList.class), anyString());
        verify(expressionList).findPagedList();

    }

    @Test
    @Order(2)
    void getUserTest() {
        UUID id = UUID.randomUUID();
        UserRegistration fakeProfile = mock(UserRegistration.class);
        getQuery(fakeProfile);
        var result = userRegistrationService.getRegistrationPendingOrThrow(id);

        assertNotNull(result);
    }

    @Test
    @Order(3)
    void getUserThrowTest() {
        UUID id = UUID.randomUUID();
        UserRegistration fakeProfile = mock(UserRegistration.class);
        getQuery(null);
        assertThrows(NotFoundException.class, () -> userRegistrationService.getRegistrationPendingOrThrow(id));
    }

    @Test
    @Order(4)
    void registerUserTest() {
        RegisterRequestDTO fakeDto = mock(RegisterRequestDTO.class);
        UserRegistration fakeProfile = mock(UserRegistration.class);

        doReturn(fakeProfile)
                .when(userRegistrationService)
                .toUserRegistrationEntity(fakeDto);
        when(keycloakService.createUserAccount(fakeDto)).thenReturn("123");
        userRegistrationService.registerUser(fakeDto);

        verify(fakeProfile).insert(transaction);
        verify(transaction).commit();
        verify(transaction).close();
    }

    @Test
    @Order(5)
    void registerUser_noKeycloak_failTest() {
        RegisterRequestDTO fakeDto = mock(RegisterRequestDTO.class);
        UserRegistration fakeProfile = mock(UserRegistration.class);
        doReturn(fakeProfile)
                .when(userRegistrationService)
                .toUserRegistrationEntity(fakeDto);
        when(keycloakService.createUserAccount(fakeDto)).thenReturn(null);
        assertThrows(ServiceException.class, () -> userRegistrationService.registerUser(fakeDto));

    }

    @Test
    @Order(6)
    void registerUser_transactionFailTest() {
        RegisterRequestDTO fakeDto = mock(RegisterRequestDTO.class);
        UserRegistration fakeProfile = mock(UserRegistration.class);
        doReturn(fakeProfile)
                .when(userRegistrationService)
                .toUserRegistrationEntity(fakeDto);
        when(keycloakService.createUserAccount(fakeDto)).thenReturn("null");
        doThrow(RuntimeException.class)
                .when(fakeProfile).update(transaction);
        assertThrows(ServiceException.class, () -> userRegistrationService.registerUser(fakeDto));

        verify(transaction,never()).commit();
        verify(transaction).close();
    }

    @Test
    @Order(7)
    void updateUserTest(){
        UserRegistration  fakeProfile = mock(UserRegistration.class);
        RegistrationStatus status = RegistrationStatus.PARTIAL_APPROVED;

        userRegistrationService.updateRegistrationRequest(fakeProfile, status);

        verify(fakeProfile).update(transaction);
        verify(transaction).commit();
        verify(transaction).close();
    }

    @Test
    @Order(7)
    void updateUserFailTest(){
        UserRegistration  fakeProfile = mock(UserRegistration.class);
        RegistrationStatus status = RegistrationStatus.PARTIAL_APPROVED;
        doThrow(RuntimeException.class)
                .when(fakeProfile).update(transaction);
        assertThrows(ServiceException.class, () -> userRegistrationService.updateRegistrationRequest(fakeProfile, status));

        verify(transaction,never()).commit();
        verify(transaction).close();
    }

    private void getQuery(UserRegistration fakeProfile) {
        Optional<UserRegistration> optional = fakeProfile == null ? Optional.empty() : Optional.of(fakeProfile);
        baseQuery();
        when(expressionList.idEq(any(UUID.class))).thenReturn(expressionList);
        when(expressionList.eq(anyString(), any(RegistrationStatus.class))).thenReturn(expressionList);
        when(expressionList.isNotNull(anyString())).thenReturn(expressionList);
        when(expressionList.findOneOrEmpty()).thenReturn(optional);
    }

    private void baseQuery() {
        when(db.find(UserRegistration.class)).thenReturn(query);
        when(query.setLabel(anyString())).thenReturn(query);
        when(query.where()).thenReturn(expressionList);
    }

}
