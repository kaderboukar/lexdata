package com.lexdata.auth.security.services;

import com.lexdata.auth.models.TokenType;
import com.lexdata.auth.models.User;
import com.lexdata.auth.models.VerificationToken;
import com.lexdata.auth.repository.UserRepository;
import com.lexdata.auth.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gère le flux complet de réinitialisation de mot de passe :
 * 1. Génération et envoi du token par email
 * 2. Validation du token et mise à jour du mot de passe
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 15;

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${lexdata.app.frontend-url}")
    private String frontendUrl;

    /**
     * Initie le processus de réinitialisation.
     *
     * <p>
     * <strong>Important (sécurité) :</strong> Même si l'email n'existe pas, on
     * retourne
     * le même message générique pour ne pas révéler d'informations sur les comptes
     * existants.
     * </p>
     *
     * @param email l'adresse email saisie par l'utilisateur
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Supprimer les tokens de reset précédents pour Cet utilisateur
            tokenRepository.deleteByUserAndType(user, TokenType.PASSWORD_RESET);

            // Générer un nouveau token sécurisé
            String tokenValue = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
            VerificationToken token = new VerificationToken(tokenValue, TokenType.PASSWORD_RESET, expiresAt, user);
            tokenRepository.save(token);

            // Construire le lien et envoyer l'email
            String resetLink = frontendUrl + "/reset-password?token=" + tokenValue;
            emailService.sendPasswordResetEmail(user, resetLink);
            log.info("Token de reset généré pour l'utilisateur : {}", user.getUsername());
        });
    }

    /**
     * Valide le token et applique le nouveau mot de passe.
     *
     * @param tokenValue  le token reçu par email
     * @param newPassword le nouveau mot de passe en clair
     * @throws IllegalArgumentException si le token est invalide, expiré ou déjà
     *                                  utilisé
     */
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        VerificationToken token = tokenRepository
                .findByTokenAndType(tokenValue, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Token de réinitialisation invalide ou inexistant."));

        if (token.isUsed()) {
            throw new IllegalArgumentException("Ce token a déjà été utilisé.");
        }

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new IllegalArgumentException(
                    "Ce token a expiré. Veuillez faire une nouvelle demande.");
        }

        // Mettre à jour le mot de passe
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marquer le token comme utilisé (invalidation)
        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Mot de passe réinitialisé avec succès pour : {}", user.getUsername());
    }
}
