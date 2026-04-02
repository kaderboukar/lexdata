package com.lexdata.juridique.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "textes_juridiques", indexes = {
    @Index(name = "idx_texte_titre", columnList = "titre"),
    @Index(name = "idx_texte_type", columnList = "type_texte")
})
@SQLRestriction("deleted = false")
@Audited
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TexteJuridique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(columnDefinition = "TEXT")
    private String titre;

    @NotBlank
    @Column(name = "reference_officielle", unique = true)
    private String referenceOfficielle;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_texte", nullable = false)
    private TypeTexte type;

    @Enumerated(EnumType.STRING)
    @Column(name = "domaine_juridique", nullable = false)
    @Builder.Default
    private LegalDomain domaine = LegalDomain.AUTRE;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_workflow", nullable = false)
    @Builder.Default
    private WorkflowStatus statut = WorkflowStatus.BROUILLON;

    @NotNull(message = "La date de signature est obligatoire")
    private LocalDate dateSignature;

    private LocalDate datePublicationJO;
    private LocalDate dateEntreeEnVigueur;

    private String journalOfficielRef;
    private String sourceOfficielle;

    @NotBlank(message = "Le contenu du texte est obligatoire")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @Column(name = "est_publie", nullable = false)
    @Builder.Default
    private Boolean estPublie = false;

    @Column(name = "est_premium", nullable = false)
    @Builder.Default
    private Boolean estPremium = false;

    @Column(columnDefinition = "boolean default false")
    @Builder.Default
    private boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum WorkflowStatus {
        BROUILLON, EN_RELECTURE, VALIDE, REJETE, PUBLIE
    }
}
