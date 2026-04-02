package com.lexdata.annuaire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalProfileDto {
    private Long id;
    private String username;
    private String photoUrl;
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
    private String statut;
    private Set<String> badges;
    private Double noteMoyenne;
    private Integer nombreAvis;
}
