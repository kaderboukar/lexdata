package com.lexdata.veille.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class AlerteRequest {
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Titre trop long")
    private String titre;

    @NotNull(message = "texteJuridiqueId est obligatoire")
    private Long texteJuridiqueId;

    private LocalDate datePublicationOfficielle;
    private LocalDate dateEntreeEnVigueur;

    @NotBlank(message = "Le résumé est obligatoire")
    @Size(max = 3000, message = "Resume trop long")
    private String resumeClarifie;

    private String pointsClesModifies;
    private String impactsPratiques;
    private String conseilsConformite;

    private String urgence; // BASSE, MOYENNE, ELEVEE
    private String status; // DRAFT, PUBLISHED, ARCHIVED
    private String eventType; // NEW_TEXT, UPDATE
    private String texteType; // LOI, DECRET, ...
    private String lienTexte;
    private String lienSynthese;

    private Set<String> domainesCibles;
    private Set<String> secteursImpactes;
}
