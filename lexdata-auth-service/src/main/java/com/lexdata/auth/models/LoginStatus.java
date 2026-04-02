package com.lexdata.auth.models;

/**
 * Statut d'une tentative de connexion pour l'audit.
 */
public enum LoginStatus {
    /**
     * Connexion réussie authentifiée.
     */
    SUCCESS,

    /**
     * Échec dû à un mot de passe incorrect ou un compte inexistant.
     */
    FAILED_BAD_CREDENTIALS,

    /**
     * Échec car l'IP a été verrouillée par le bouclier anti-brute-force.
     */
    FAILED_LOCKED,

    /**
     * Compte existant mais désactivé (ex. e-mail non vérifié).
     */
    FAILED_DISABLED
}
