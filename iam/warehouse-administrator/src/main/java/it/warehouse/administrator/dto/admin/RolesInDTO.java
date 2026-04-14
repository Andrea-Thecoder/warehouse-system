package it.warehouse.administrator.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class RolesInDTO {

    @NotEmpty(message = "The approved roles list can not be empty")
    private Set<String> approvedRoles;
}
