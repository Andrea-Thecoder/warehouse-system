package it.warehouse.administrator.dto.role;

import it.warehouse.administrator.model.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleRoleTypeDTO {

    private String id;
    private String label;

    public static SimpleRoleTypeDTO of(RoleType roleType) {
        SimpleRoleTypeDTO dto = new SimpleRoleTypeDTO();
        dto.id = roleType.getId();
        dto.label = roleType.getLabel();
        return dto;
    }

}
