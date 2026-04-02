package com.lexdata.auth.models;

/**
 * Type de token de vérification.
 * Utilisé par VerificationToken pour distinguer les usages.
 */
public enum TokenType {
    /** Token de réinitialisation de mot de passe (TTL 15 min) */
    PASSWORD_RESET,

    /** Token de vérification d'adresse email après inscription */
    EMAIL_VERIFICATION
}
