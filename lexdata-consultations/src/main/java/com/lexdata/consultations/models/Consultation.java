package com.lexdata.consultations.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String clientUsername;

    private String juristeUsername;

    @NotBlank
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ConsultationType type = ConsultationType.CHAT;

    @Enumerated(EnumType.STRING)
    private ConsultationStatus status = ConsultationStatus.EN_ATTENTE;

    private LocalDateTime dateRendezVous;

    private int dureeMinutes = 30;

    @Column(columnDefinition = "TEXT")
    private String compteRendu;

    private String meetingLink;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum ConsultationType {
        CHAT, VISIO, TELEPHONE
    }

    public enum ConsultationStatus {
        EN_ATTENTE, CONFIRME, TERMINE, ANNULE
    }

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClientUsername() { return clientUsername; }
    public void setClientUsername(String clientUsername) { this.clientUsername = clientUsername; }
    public String getJuristeUsername() { return juristeUsername; }
    public void setJuristeUsername(String juristeUsername) { this.juristeUsername = juristeUsername; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ConsultationType getType() { return type; }
    public void setType(ConsultationType type) { this.type = type; }
    public ConsultationStatus getStatus() { return status; }
    public void setStatus(ConsultationStatus status) { this.status = status; }
    public LocalDateTime getDateRendezVous() { return dateRendezVous; }
    public void setDateRendezVous(LocalDateTime dateRendezVous) { this.dateRendezVous = dateRendezVous; }
    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }
    public String getCompteRendu() { return compteRendu; }
    public void setCompteRendu(String compteRendu) { this.compteRendu = compteRendu; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
}
