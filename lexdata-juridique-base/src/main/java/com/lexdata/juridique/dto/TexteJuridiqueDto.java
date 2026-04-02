package com.lexdata.juridique.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TexteJuridiqueDto {
    private Long id;
    private String titre;
    private String referenceOfficielle;
    private String type;
    private String domaine;
    private String statut;
    private LocalDate dateSignature;
    private LocalDate datePublicationJO;
    private LocalDate dateEntreeEnVigueur;
    private String journalOfficielRef;
    private String sourceOfficielle;
    private String contenu;
    private boolean estPublie;
    private boolean estPremium;
}
