package com.lexdata.calendrier.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_calendar_configs")
public class UserCalendarConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String typeSujet; // ENTREPRISE, PARTICULIER
    
    // Pour entreprise
    private LocalDate dateClotureExercice;
    private String regimeFiscal; // REEL_SIMPLIFIE, REEL_NORMAL, MICRO
    private int nbSalaries;
    private LocalDate dateCreationEntreprise;

    // Pour particulier
    private LocalDate dateNaissance;
    private String situationMatrimoniale;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTypeSujet() { return typeSujet; }
    public void setTypeSujet(String typeSujet) { this.typeSujet = typeSujet; }
    public LocalDate getDateClotureExercice() { return dateClotureExercice; }
    public void setDateClotureExercice(LocalDate dateClotureExercice) { this.dateClotureExercice = dateClotureExercice; }
    public String getRegimeFiscal() { return regimeFiscal; }
    public void setRegimeFiscal(String regimeFiscal) { this.regimeFiscal = regimeFiscal; }
    public int getNbSalaries() { return nbSalaries; }
    public void setNbSalaries(int nbSalaries) { this.nbSalaries = nbSalaries; }
    public LocalDate getDateCreationEntreprise() { return dateCreationEntreprise; }
    public void setDateCreationEntreprise(LocalDate dateCreationEntreprise) { this.dateCreationEntreprise = dateCreationEntreprise; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getSituationMatrimoniale() { return situationMatrimoniale; }
    public void setSituationMatrimoniale(String situationMatrimoniale) { this.situationMatrimoniale = situationMatrimoniale; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
}
