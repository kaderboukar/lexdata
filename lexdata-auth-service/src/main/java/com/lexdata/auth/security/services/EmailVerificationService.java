package com.lexdata.auth.security.services;

import com.lexdata.auth.models.TokenType;
import com.lexdata.auth.models.User;
import com.lexdata.auth.models.VerificationToken;
import com.lexdata.auth.repository.UserRepository;
import com.lexdata.auth.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gère le flux de vérification d'adresse email :
 * 1. Envoi du lien de vérification lors de l'inscription
 * 2. Activation du compte sur clic du lien
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    // Durée de validité du token de vérification (plus longue que le reset)
    private static final int TOKEN_EXPIRY_HOURS = 24;

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${lexdata.app.frontend-url}")
    private String frontendUrl;

    /**
     * Génère un token de vérification et envoie l'email d'activation.
     * Appelé immédiatement après l'inscription d'un nouveau compte.
     *
     * @param user l'utilisateur fraîchement créé (active = false)
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        // Supprimer tout token de vérification précédent (renvoi possible)
        tokenRepository.deleteByUserAndType(user, TokenType.EMAIL_VERIFICATION);

        // Générer un token sécurisé avec 24h de validité
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);
        VerificationToken token = new VerificationToken(tokenValue, TokenType.EMAIL_VERIFICATION, expiresAt, user);
        tokenRepository.save(token);

        // Construire le lien de vérification
        String verifyLink = frontendUrl + "/verify-email?token=" + tokenValue;
        emailService.sendEmailVerificationEmail(user, verifyLink);
        log.info("Email de vérification envoyé à : {}", user.getEmail());
    }

    /**
     * Valide le token et active le compte.
     *
     * @param tokenValue le token reçu via le lien email
     * @throws IllegalArgumentException si le token est invalide, expiré ou déjà
     *                                  utilisé
     */
    @Transactional
    public void verifyEmail(String tokenValue) {
        VerificationToken token = tokenRepository
                .findByTokenAndType(tokenValue, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Lien de vérification invalide ou inexistant."));

        if (token.isUsed()) {
            throw new IllegalArgumentException(
                    "Ce lien a déjà été utilisé. Votre compte est déjà activé.");
        }

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new IllegalArgumentException(
                    "Ce lien a expiré. Veuillez demander un nouveau lien de vérification.");
        }

        // Activer le compte utilisateur
        User user = token.getUser();
        user.setActive(true);
        user.setEmailVerified(true);
        userRepository.save(user);

        // Invalider le token
        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Compte activé avec succès pour : {}", user.getUsername());
    }

    /**
     * Renvoie un email de vérification pour un compte non encore activé.
     * Utile si l'utilisateur n'a pas reçu le premier email.
     *
     * @param email l'adresse email du compte
     * @throws IllegalArgumentException si l'email n'existe pas ou si le compte est
     *                                  déjà actif
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte trouvé pour cet email."));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Ce compte est déjà vérifié.");
        }

        sendVerificationEmail(user);
        log.info("Email de vérification renvoyé à : {}", email);
    }
}
