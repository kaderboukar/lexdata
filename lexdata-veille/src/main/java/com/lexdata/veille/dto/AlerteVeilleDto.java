package com.lexdata.veille.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteVeilleDto {
    private Long id;
    private String titre;
    private Long texteJuridiqueId;
    private LocalDate datePublicationOfficielle;
    private LocalDate dateEntreeEnVigueur;
    private String resumeClarifie;
    private String pointsClesModifies;
    private String impactsPratiques;
    private String conseilsConformite;
    private String eventType;
    private String texteType;
    private String lienTexte;
    private String lienSynthese;
    private String urgence;
    private String statut;
    private Set<String> domainesCibles;
    private Set<String> secteursImpactes;
    private LocalDateTime dateCreation;
}
