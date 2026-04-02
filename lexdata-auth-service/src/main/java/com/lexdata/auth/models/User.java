package com.lexdata.auth.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where; // Pour le soft delete automatique

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "\"users\"", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
}, indexes = {
        @Index(name = "idx_user_email", columnList = "email"), // Recherche rapide par email
        @Index(name = "idx_user_username", columnList = "username") // Recherche rapide par login
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// IMPORTANT : Hibernate filtrera automatiquement les utilisateurs supprimés
// dans les recherches normales
// @Where(clause = "is_deleted = false") <-- Note: Dans Hibernate 6+ (Spring
// Boot 3.3), l'annotation @Where est dépréciée, on utilise @SQLRestriction
@org.hibernate.annotations.SQLRestriction("is_deleted = false")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 20, message = "Le nom d'utilisateur doit contenir entre 3 et 20 caractères")
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 50)
    @Email(message = "Format d'email invalide")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(max = 120)
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    // --- Nouveaux Champs ---

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Size(max = 20)
    @Column(nullable = false)
    private String telephone;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50)
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50)
    @Column(name = "last_name")
    private String lastName;

    @Builder.Default
    @Column(name = "pending_validation", columnDefinition = "boolean default false")
    private boolean pendingValidation = false;

    // --- Sécurité & État ---

    @Builder.Default
    @Column(columnDefinition = "boolean default false")
    private boolean active = false; // Activé seulement après vérification email

    @Builder.Default
    @Column(name = "email_verified", columnDefinition = "boolean default false")
    private boolean emailVerified = false; // Vérification email obligatoire à l'inscription

    @Builder.Default
    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean deleted = false; // SOFT DELETE : On ne supprime jamais rien physiquement

    // --- MFA (Multi-Factor Authentication) ---
    // Obligatoire pour les agents selon le document source
    @Builder.Default
    @Column(name = "mfa_enabled", columnDefinition = "boolean default false")
    private boolean mfaEnabled = false;

    @JsonIgnore
    private String mfaSecret; // Clé secrète Google Authenticator

    // --- Relations ---

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // --- Audit & Traçabilité (Indispensable pour le juridique) ---

    @CreationTimestamp
    @Column(updatable = false, name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Utile pour savoir quel admin a validé ce compte agent
    @Column(name = "created_by")
    private String createdBy;

    // Constructeur utilitaire
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.active = true;
        this.deleted = false;
        this.mfaEnabled = false;
    }
}