package it.warehouse.administrator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Username must be valorized")
    private String username;

    @NotBlank(message = "Email must be valorized")
    @Email(message = "Email must be valorized")
    private String email;

    @NotBlank(message = "Nome must be valorized")
    private String firstName;

    @NotBlank(message = "Cognome must be valorized")
    private String lastName;

    @NotBlank(message = "Password must be valorized")
    @Size(min = 8, message = "La password  must have min 8 characters")
    private String password;

    @NotEmpty(message = "You must select at least one role.")
    private List<String> requestedRoleIds;
}