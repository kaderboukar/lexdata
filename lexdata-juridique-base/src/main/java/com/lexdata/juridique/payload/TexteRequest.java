package com.lexdata.juridique.payload;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TexteRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre est trop long")
    private String titre;

    @NotBlank(message = "La référence officielle est obligatoire")
    @Size(max = 120, message = "La reference officielle est trop longue")
    private String referenceOfficielle; // Ex: "Loi 2024-01"

    @NotBlank(message = "Le type de texte est obligatoire (LOI, DECRET...)")
    private String type;

    @NotBlank(message = "Le domaine juridique est obligatoire")
    private String domaine;

    @NotNull(message = "La date de signature est obligatoire")
    private LocalDate dateSignature;

    private LocalDate datePublicationJO;
    private LocalDate dateEntreeEnVigueur;

    @Size(max = 120, message = "La reference du Journal Officiel est trop longue")
    private String journalOfficielRef;
    @Size(max = 255, message = "La source officielle est trop longue")
    private String sourceOfficielle;

    @NotBlank(message = "Le contenu est obligatoire")
    @Size(max = 100000, message = "Le contenu est trop long")
    private String contenu;

    private boolean estPremium;
}
