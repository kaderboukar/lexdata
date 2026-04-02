package com.lexdata.synthese.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "synthese_versions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyntheseVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_id", nullable = false)
    @JsonIgnore
    private FicheSynthetique fiche;

    @Column(columnDefinition = "TEXT")
    private String contenuJson; // Snapshot des champs au format JSON ou texte concaténé

    private String agentEmail;

    private String commentaireVersion;

    private Long texteJuridiqueVersionId; // Lien vers une version précise du texte

    @CreationTimestamp
    private LocalDateTime dateVersion;
}
