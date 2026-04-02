package com.lexdata.auth.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.Pattern;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20, message = "Le nom d'utilisateur doit faire entre 3 et 20 caractères")
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Size(max = 20)
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Le numero de telephone est invalide")
    private String telephone;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Size(min = 6, max = 40, message = "Le mot de passe doit faire entre 6 et 40 caractères")
    private String password;
}