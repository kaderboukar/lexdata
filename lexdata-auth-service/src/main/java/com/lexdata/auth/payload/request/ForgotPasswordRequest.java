package com.lexdata.auth.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Requête pour initier la réinitialisation du mot de passe.
 * L'utilisateur fournit uniquement son adresse email.
 */
@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
}
