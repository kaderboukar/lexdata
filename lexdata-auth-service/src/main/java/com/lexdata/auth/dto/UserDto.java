package com.lexdata.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDateTime; // Décommentez si vous utilisez la date de création

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String telephone;

    // On renvoie une simple liste de chaînes de caractères (ex: ["ROLE_AVOCAT"])
    // C'est beaucoup plus léger et facile à lire pour React que d'envoyer l'objet Role complet.
    private List<String> roles;

    private LocalDateTime createdAt; // Décommentez si vous avez ce champ dans votre entité User
}
