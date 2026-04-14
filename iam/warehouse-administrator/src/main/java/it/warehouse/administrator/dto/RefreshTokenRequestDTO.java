package it.warehouse.administrator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token obbligatorio")
    private String refreshToken;
}