package com.lexdata.juridique.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "texte_versions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "texte_id", nullable = false)
    private TexteJuridique texte;

    private String versionLabel; // Ex: "Consolidation au 20/02/2026"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @CreationTimestamp
    private LocalDateTime dateVersion;

    private String modificationSummary; // Résumé des changements
}
