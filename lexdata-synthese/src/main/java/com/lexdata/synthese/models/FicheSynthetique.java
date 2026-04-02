package com.lexdata.synthese.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fiches_synthetiques", indexes = {
        @Index(name = "idx_fiche_legal_text", columnList = "texte_juridique_id"),
        @Index(name = "idx_fiche_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheSynthetique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Column(nullable = false, length = 255)
    private String titre;

    @Column(name = "texte_juridique_id", nullable = false)
    private Long texteJuridiqueId; // Lien vers jurídico-base

    @NotBlank(message = "Le contenu de la synthese est obligatoire")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(columnDefinition = "TEXT")
    private String objectifPrincipal;

    @Column(columnDefinition = "TEXT")
    private String changementsCles;

    @Column(columnDefinition = "TEXT")
    private String obligations;

    @Column(columnDefinition = "TEXT")
    private String sanctions;

    @Column(columnDefinition = "TEXT")
    private String conseilsPratiques;

    @Column(columnDefinition = "TEXT")
    private String exemplesConcrets;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SyntheseStatus status = SyntheseStatus.DRAFT;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum SyntheseStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }
}
