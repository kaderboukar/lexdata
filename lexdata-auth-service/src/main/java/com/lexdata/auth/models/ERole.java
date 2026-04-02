package com.lexdata.auth.models;

public enum ERole {
    ROLE_USER, // Citoyen lambda
    ROLE_JURISTE, // Peut publier tribune, commenter veille
    ROLE_AVOCAT, // Profil annuaire, consultations
    ROLE_AGENT_ADMIN, // Back-office (saisie, validation profils)
    ROLE_SUPER_ADMIN // Administrateur technique complet
}