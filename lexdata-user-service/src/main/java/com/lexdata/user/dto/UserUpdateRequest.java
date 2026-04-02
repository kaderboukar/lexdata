package com.lexdata.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.lexdata.user.models.LegalDomain;
import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateRequest {
    // --- Infos de Profil ---
    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(max = 120, message = "Le nom complet est trop long")
    private String fullName;
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Le numero de telephone est invalide")
    private String phoneNumber;
    @Size(max = 80, message = "La ville est trop longue")
    private String city;
    @Size(max = 255, message = "URL de photo trop longue")
    private String profilePictureUrl;
    @Size(max = 5, message = "Langue invalide")
    private String preferredLanguage;
    @Size(max = 1000, message = "Bio trop longue")
    private String bio;

    // Champs Juriste/Avocat
    private Set<LegalDomain> specialties;
    @Size(max = 120, message = "Titre professionnel trop long")
    private String professionalTitle;
    @Size(max = 120, message = "Disponibilite trop longue")
    private String availability;
    @Size(max = 100, message = "Barreau trop long")
    private String barreau;
    @Size(max = 100, message = "Numero de toque trop long")
    private String numeroToque;

    // Champs Entreprise
    @Size(max = 150, message = "Nom de societe trop long")
    private String companyName;
    @Pattern(regexp = "^[A-Za-z0-9\\-_/]{5,30}$", message = "NIF invalide")
    private String nif;
    private Integer employeeCount;

    // --- Préférences ---
    private Set<LegalDomain> followedTopics;
    private Set<String> alertKeywords;
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean smsEnabled;
    @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+$", message = "Timezone invalide")
    private String timezone;
}
