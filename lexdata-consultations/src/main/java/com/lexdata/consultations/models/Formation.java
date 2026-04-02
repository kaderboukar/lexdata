package com.lexdata.consultations.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "formations")
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    private String formateur;

    private LocalDateTime dateSession;

    private int placesVisibles = 50;

    private double prix = 0.0;

    private boolean isReplayAvailable = false;

    private String replayUrl;

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
    public String getFormateur() { return formateur; }
    public void setFormateur(String formateur) { this.formateur = formateur; }
    public LocalDateTime getDateSession() { return dateSession; }
    public void setDateSession(LocalDateTime dateSession) { this.dateSession = dateSession; }
    public int getPlacesVisibles() { return placesVisibles; }
    public void setPlacesVisibles(int placesVisibles) { this.placesVisibles = placesVisibles; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public boolean isReplayAvailable() { return isReplayAvailable; }
    public void setReplayAvailable(boolean replayAvailable) { isReplayAvailable = replayAvailable; }
    public String getReplayUrl() { return replayUrl; }
    public void setReplayUrl(String replayUrl) { this.replayUrl = replayUrl; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
}
