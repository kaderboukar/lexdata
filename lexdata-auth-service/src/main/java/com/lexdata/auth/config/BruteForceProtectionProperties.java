package com.lexdata.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés de configuration pour la protection anti-brute-force.
 * Lues depuis application.yml sous le préfixe "lexdata.security.brute-force".
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "lexdata.security.brute-force")
public class BruteForceProtectionProperties {

    /**
     * Nombre maximum de tentatives échouées avant verrouillage de l'IP.
     * Défaut : 5
     */
    private int maxAttempts = 5;

    /**
     * Durée de verrouillage en minutes.
     * Défaut : 15 minutes
     */
    private int lockDurationMinutes = 15;
}
