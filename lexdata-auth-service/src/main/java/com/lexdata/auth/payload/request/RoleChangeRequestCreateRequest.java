package com.lexdata.auth.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleChangeRequestCreateRequest {
    @NotBlank(message = "Le role demande est obligatoire")
    @Size(max = 40, message = "Le role demande est invalide")
    private String requestedRole;
}
