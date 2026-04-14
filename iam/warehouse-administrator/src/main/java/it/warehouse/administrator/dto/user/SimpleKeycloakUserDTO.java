package it.warehouse.administrator.dto.user;

import it.warehouse.administrator.dto.role.SimpleRoleTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleKeycloakUserDTO {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private List<SimpleRoleTypeDTO> roles;

    public static SimpleKeycloakUserDTO of(UserRepresentation user, List<SimpleRoleTypeDTO> roles) {
        SimpleKeycloakUserDTO dto = new SimpleKeycloakUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(roles);
        return dto;
    }
}