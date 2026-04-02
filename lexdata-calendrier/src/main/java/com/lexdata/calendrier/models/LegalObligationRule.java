package com.lexdata.calendrier.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "legal_obligation_rules")
public class LegalObligationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String categorie; // FISCALE, SOCIALE, COMMERCIALE, ADMINISTRATIVE

    private String frequence; // MENSUELLE, TRIMESTRIELLE, ANNUELLE, PONCTUELLE

    private int delaiJours; // ex: 15 pour "jusqu'au 15 du mois suivant"
    
    private String regleCalcul; // ex: D+15_MOIS_SUIVANT, CLOTURE+4_MOIS

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }
    public int getDelaiJours() { return delaiJours; }
    public void setDelaiJours(int delaiJours) { this.delaiJours = delaiJours; }
    public String getRegleCalcul() { return regleCalcul; }
    public void setRegleCalcul(String regleCalcul) { this.regleCalcul = regleCalcul; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
}
