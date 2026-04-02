package com.lexdata.consultations.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_creation_packs")
public class CompanyCreationPack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String ownerUsername;

    @NotBlank
    private String nomEntrepriseVise;

    private String formeJuridique; // SARL, SAU, etc.

    @Enumerated(EnumType.STRING)
    private PackStatus status = PackStatus.BROUILLON;

    private String assignedJuriste;

    @Column(columnDefinition = "TEXT")
    private String checklistStatus; // JSON ou texte structuré pour les étapes (RCCM, NIF, etc.)

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum PackStatus {
        BROUILLON, EN_COURS, DEPOSE, APPROUVE, TERMINE
    }

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public String getNomEntrepriseVise() { return nomEntrepriseVise; }
    public void setNomEntrepriseVise(String nomEntrepriseVise) { this.nomEntrepriseVise = nomEntrepriseVise; }
    public String getFormeJuridique() { return formeJuridique; }
    public void setFormeJuridique(String formeJuridique) { this.formeJuridique = formeJuridique; }
    public PackStatus getStatus() { return status; }
    public void setStatus(PackStatus status) { this.status = status; }
    public String getAssignedJuriste() { return assignedJuriste; }
    public void setAssignedJuriste(String assignedJuriste) { this.assignedJuriste = assignedJuriste; }
    public String getChecklistStatus() { return checklistStatus; }
    public void setChecklistStatus(String checklistStatus) { this.checklistStatus = checklistStatus; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
}
