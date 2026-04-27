package it.warehouse.administrator.dto;

import it.warehouse.administrator.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Username must be provided")
    private String username;

    @NotBlank(message = "Email must be provided")
    @Email(message = "Email must be a valid address")
    private String email;

    @NotBlank(message = "First name must be provided")
    private String firstName;

    @NotBlank(message = "Last name must be provided")
    private String lastName;

    @NotBlank(message = "Password must be provided")
    @StrongPassword
    private String password;

    @NotEmpty(message = "You must select at least one role.")
    private List<String> requestedRoleIds;
}