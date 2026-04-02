package com.lexdata.veille.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "alertes_veille", indexes = {
        @Index(name = "idx_alerte_text_event", columnList = "texte_juridique_id,event_type"),
        @Index(name = "idx_alerte_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteVeille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @Column(name = "texte_juridique_id", nullable = false)
    private Long texteJuridiqueId; // Lien vers lexdata-juridique-base

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    @Builder.Default
    private EventType eventType = EventType.NEW_TEXT;

    @Column(name = "texte_type", length = 80)
    private String texteType;

    private LocalDate datePublicationOfficielle;
    private LocalDate dateEntreeEnVigueur;

    @NotBlank(message = "Le résumé est obligatoire")
    @Column(columnDefinition = "TEXT")
    private String resumeClarifie;

    @Column(columnDefinition = "TEXT")
    private String pointsClesModifies;

    @Column(columnDefinition = "TEXT")
    private String impactsPratiques;

    @Column(columnDefinition = "TEXT")
    private String conseilsConformite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UrgenceLevel urgence = UrgenceLevel.BASSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AlertStatus status = AlertStatus.DRAFT;

    @Column(name = "lien_texte", length = 255)
    private String lienTexte;

    @Column(name = "lien_synthese", length = 255)
    private String lienSynthese;

    @ElementCollection
    @CollectionTable(name = "alerte_domaines", joinColumns = @JoinColumn(name = "alerte_id"))
    @Column(name = "domaine")
    @Builder.Default
    private Set<String> domainesCibles = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "alerte_secteurs", joinColumns = @JoinColumn(name = "alerte_id"))
    @Column(name = "secteur")
    @Builder.Default
    private Set<String> secteursImpactes = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum UrgenceLevel {
        BASSE, MOYENNE, ELEVEE
    }

    public enum AlertStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    public enum EventType {
        NEW_TEXT, UPDATE
    }
}
