package it.warehouse.administrator.dto.user;

import it.warehouse.administrator.dto.role.SimpleRoleTypeDTO;
import it.warehouse.administrator.model.UserRegistration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleUserRegistrationDTO {

    private UUID id;
    private String fullname;
    private List<SimpleRoleTypeDTO> roles;

    public static SimpleUserRegistrationDTO of (UserRegistration userRegistration) {
        SimpleUserRegistrationDTO dto = new SimpleUserRegistrationDTO();
        dto.setId(userRegistration.getId());
        dto.setFullname(userRegistration.getFullname());
        dto.setRoles(userRegistration.getRequestedRoleType().stream().map(SimpleRoleTypeDTO::of).toList());
        return dto;

    }


}
