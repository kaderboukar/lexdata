package com.lexdata.synthese.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheSynthetiqueDto {
    private Long id;
    private String titre;
    private Long texteJuridiqueId;
    private String content;
    private Integer version;
    private String objectifPrincipal;
    private String changementsCles;
    private String obligations;
    private String sanctions;
    private String conseilsPratiques;
    private String exemplesConcrets;
    private String status;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
