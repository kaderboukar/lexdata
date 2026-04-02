package com.lexdata.calendrier.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private LegalObligationRule rule;

    private String titre;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime dateEcheance;
    
    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.A_VENIR;

    private boolean manuel = false;
    
    private String sourceService; // ex: CONTRATS, VEILLE, JURIDIQUE

    @CreationTimestamp
    private LocalDateTime dateCreation;

    public enum EventStatus {
        A_VENIR, TRAITE, REPORTE, EXPIRE
    }

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LegalObligationRule getRule() { return rule; }
    public void setRule(LegalObligationRule rule) { this.rule = rule; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDateTime dateEcheance) { this.dateEcheance = dateEcheance; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public boolean isManuel() { return manuel; }
    public void setManuel(boolean manuel) { this.manuel = manuel; }
    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }
    public LocalDateTime getDateCreation() { return dateCreation; }
}
