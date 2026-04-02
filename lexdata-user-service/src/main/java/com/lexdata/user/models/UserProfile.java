package com.lexdata.user.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien avec le service Auth (on stocke le username ou l'ID venant du token)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Le nom complet est obligatoire")
    private String fullName;

    private String phoneNumber;

    private String city; // Ex: Niamey, Maradi...

    private String profilePictureUrl;

    @Builder.Default
    private String preferredLanguage = "FR"; // FR, HA, ZA...

    @Column(columnDefinition = "TEXT")
    private String bio;

    // --- Champs Spécifiques JURISTE / AVOCAT ---
    @ElementCollection(targetClass = LegalDomain.class)
    @CollectionTable(name = "user_specialties", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialty")
    @Builder.Default
    private java.util.Set<LegalDomain> specialties = new java.util.HashSet<>();

    private String professionalTitle; // Ex: Barreau de Niamey
    private String availability; // Ex: Lun-Ven 08h-18h
    private String barreau;
    private String numeroToque;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    // --- Champs Spécifiques ENTREPRISE ---
    private String companyName;
    private String nif; // Numéro d'Identification Fiscale
    private Integer employeeCount;

    // Gestion de l'abonnement (Source de vérité: subscription-service, ici pour le
    // cache/vue)
    @Column(name = "subscription_type")
    @Builder.Default
    private String subscriptionType = "FREE"; // FREE, PREMIUM, PRO

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}