package com.lexdata.annuaire.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class ProfileRequest {
    @NotBlank
    private String titre;
    private String cabinet;
    private String adresse;
    private String ville;
    private String telephone;
    private String email;
    private String siteWeb;
    private String numeroOrdre;
    private Set<String> expertises;
    private Set<String> langues;
    private String bio;
    private String tarifsIndicatifs;
    private String horairesConsultation;
}
