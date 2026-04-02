package com.lexdata.auth.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Génère getters/setters automatiquement
public class LoginRequest {
    @NotBlank(message = "Le nom d'utilisateur ne peut pas être vide")
    private String username;

    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    private String password;
}