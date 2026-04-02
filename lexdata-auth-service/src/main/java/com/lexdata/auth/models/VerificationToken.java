package com.lexdata.auth.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Token à usage unique pour le reset de mot de passe et la vérification email.
 *
 * <p>
 * Partage la même table pour les deux types de tokens. Chaque token :
 * <ul>
 * <li>Est un UUID généré aléatoirement (imprévisible)</li>
 * <li>Expire après 15 minutes</li>
 * <li>Ne peut être utilisé qu'une seule fois ({@code used = true} après
 * validation)</li>
 * </ul>
 */
@Entity
@Table(name = "verification_tokens", indexes = {
        @Index(name = "idx_vtoken_token", columnList = "token"),
        @Index(name = "idx_vtoken_user_type", columnList = "user_id, token_type")
})
@Data
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vtoken_seq")
    @SequenceGenerator(name = "vtoken_seq", sequenceName = "vtoken_seq", allocationSize = 1)
    private Long id;

    /**
     * Valeur du token UUID aléatoire envoyé par email.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /**
     * Type de token : PASSWORD_RESET ou EMAIL_VERIFICATION.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 30)
    private TokenType type;

    /**
     * Date d'expiration (15 minutes après la création).
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Flag usage unique : mis à true dès que le token est consommé.
     */
    @Column(nullable = false)
    private boolean used = false;

    /**
     * Utilisateur propriétaire du token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public VerificationToken(String token, TokenType type, LocalDateTime expiresAt, User user) {
        this.token = token;
        this.type = type;
        this.expiresAt = expiresAt;
        this.user = user;
        this.used = false;
        this.createdAt = LocalDateTime.now();
    }

    /** Vérifie si le token est encore valide (non expiré et non utilisé). */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
}
