package it.warehouse.administrator.security;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JsonWebToken jwtToken;

    @InjectMocks
    private JwtService jwtService;

    @Test
    void hasRole_whenRolePresent_returnsTrue() {
        stubRoles(List.of("ADMIN", "WAREHOUSE_OPERATOR"));
        assertThat(jwtService.hasRole("ADMIN")).isTrue();
    }

    @Test
    void hasRole_whenRoleAbsent_returnsFalse() {
        stubRoles(List.of("WAREHOUSE_OPERATOR"));
        assertThat(jwtService.hasRole("ADMIN")).isFalse();
    }

    @Test
    void hasRole_roleNamesAreCaseNormalized() {
        stubRoles(List.of("admin"));
        assertThat(jwtService.hasRole("ADMIN")).isTrue();
    }

    @Test
    void hasRole_roleNameWithExtraSpaces_isStripped() {
        stubRoles(List.of("  ADMIN  "));
        assertThat(jwtService.hasRole("ADMIN")).isTrue();
    }


    @Test
    void isAdmin_whenAdminRolePresent_returnsTrue() {
        stubRoles(List.of("ADMIN"));
        assertThat(jwtService.isAdmin()).isTrue();
    }

    @Test
    void isAdmin_whenAdminRoleAbsent_returnsFalse() {
        stubRoles(List.of("WAREHOUSE_OPERATOR", "TRANSPORT_ADMIN"));
        assertThat(jwtService.isAdmin()).isFalse();
    }

    @Test
    void hasRole_whenRealmAccessClaimIsNull_returnsFalse() {
        when(jwtToken.getClaim("realm_access")).thenReturn(null);
        assertThat(jwtService.hasRole("ADMIN")).isFalse();
    }

    @Test
    void hasRole_whenRolesListIsEmpty_returnsFalse() {
        when(jwtToken.getClaim("realm_access")).thenReturn(Map.of("roles", List.of()));
        assertThat(jwtService.hasRole("ADMIN")).isFalse();
    }

    @Test
    void hasRole_whenRolesKeyMissingFromClaim_returnsFalse() {
        when(jwtToken.getClaim("realm_access")).thenReturn(Map.of());
        assertThat(jwtService.hasRole("ADMIN")).isFalse();
    }

    private void stubRoles(List<String> roles) {
        when(jwtToken.getClaim("realm_access")).thenReturn(Map.of("roles", roles));
    }
}