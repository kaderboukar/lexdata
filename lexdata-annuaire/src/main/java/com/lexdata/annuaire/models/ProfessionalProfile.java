package com.lexdata.annuaire.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "professional_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'utilisateur est obligatoire")
    private String username; // Lien vers auth-service via username

    private String photoUrl;
    
    @NotBlank(message = "Le titre professionnel est obligatoire")
    private String titre; // Maître, Docteur, etc.

    private String cabinet;
    private String adresse;
    private String ville;
    private String telephone;
    private String email;
    private String siteWeb;
    private String numeroOrdre; // Inscription barreau/ordre

    @ElementCollection
    @CollectionTable(name = "pro_expertises", joinColumns = @JoinColumn(name = "pro_id"))
    @Column(name = "expertise")
    @Builder.Default
    private Set<String> expertises = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "pro_langues", joinColumns = @JoinColumn(name = "pro_id"))
    @Column(name = "langue")
    @Builder.Default
    private Set<String> langues = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String tarifsIndicatifs;
    private String horairesConsultation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ValidationStatus statut = ValidationStatus.EN_ATTENTE;

    @ElementCollection
    @CollectionTable(name = "pro_badges", joinColumns = @JoinColumn(name = "pro_id"))
    @Column(name = "badge")
    @Builder.Default
    private Set<String> badges = new HashSet<>();

    private Double noteMoyenne = 0.0;
    private Integer nombreAvis = 0;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum ValidationStatus {
        EN_ATTENTE, VALIDE, REJETE, SUSPENDU
    }
}
