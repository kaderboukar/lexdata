package com.lexdata.synthese.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SyntheseRequest {
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre est trop long")
    private String titre;

    @NotNull(message = "legalTextId est obligatoire")
    private Long texteJuridiqueId;

    @NotBlank(message = "Le contenu est obligatoire")
    @Size(max = 50000, message = "Le contenu est trop long")
    private String content;

    @NotNull(message = "La version est obligatoire")
    private Integer version;

    @NotBlank(message = "Le status est obligatoire")
    private String status;

    private String objectifPrincipal;
    private String changementsCles;
    private String obligations;
    private String sanctions;
    private String conseilsPratiques;
    private String exemplesConcrets;

    private String commentaireVersion; // Pour l'historique
    private Long texteJuridiqueVersionId; // La version du texte juridique liée à cette version de fiche
}
