package com.lexdata.auth.security.services;

import com.lexdata.auth.config.BruteForceProtectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * Service de protection anti-brute-force.
 *
 * <p>
 * Utilise un cache Caffeine pour mémoriser le nombre de tentatives
 * d'authentification
 * échouées par adresse IP. Après {@code maxAttempts} échecs consécutifs, l'IP
 * est
 * considérée comme bloquée pendant {@code lockDurationMinutes} minutes.
 * </p>
 *
 * <p>
 * Le verrouillage est automatiquement levé à l'expiration du cache
 * (expireAfterWrite).
 * </p>
 */
@Slf4j
@Service
public class LoginAttemptService {

    private final int maxAttempts;
    private final Cache<String, Integer> attemptsCache;

    private String keyFor(String ip, String username) {
        return ip + "|" + (username == null ? "" : username.toLowerCase());
    }

    public LoginAttemptService(BruteForceProtectionProperties properties) {
        this.maxAttempts = properties.getMaxAttempts();
        this.attemptsCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(properties.getLockDurationMinutes(), TimeUnit.MINUTES)
                .build();
    }

    /**
     * À appeler après une authentification RÉUSSIE.
     * Remet à zéro le compteur de l'IP (supprime l'entrée du cache).
     *
     * @param ip l'adresse IP du client
     */
    public void loginSucceeded(String ip, String username) {
        attemptsCache.invalidate(ip);
        attemptsCache.invalidate(keyFor(ip, username));
        log.debug("Tentatives réinitialisées pour l'IP : {} et utilisateur : {}", ip, username);
    }

    /**
     * À appeler après une authentification ÉCHOUÉE.
     * Incrémente le compteur de tentatives de l'IP.
     *
     * @param ip l'adresse IP du client
     */
    public void loginFailed(String ip, String username) {
        int attempts = attemptsCache.get(ip, k -> 0) + 1;
        attemptsCache.put(ip, attempts);
        String scopedKey = keyFor(ip, username);
        int scopedAttempts = attemptsCache.get(scopedKey, k -> 0) + 1;
        attemptsCache.put(scopedKey, scopedAttempts);
        log.warn("Tentative échouée #{} pour IP : {} et utilisateur : {}", scopedAttempts, ip, username);
    }

    /**
     * Vérifie si une IP est actuellement bloquée.
     *
     * @param ip l'adresse IP à vérifier
     * @return {@code true} si le nombre de tentatives >= maxAttempts
     */
    public boolean isBlocked(String ip, String username) {
        Integer attempts = attemptsCache.getIfPresent(ip);
        Integer scopedAttempts = attemptsCache.getIfPresent(keyFor(ip, username));
        return (attempts != null && attempts >= maxAttempts)
                || (scopedAttempts != null && scopedAttempts >= maxAttempts);
    }

    /**
     * Retourne le nombre de tentatives restantes avant blocage.
     *
     * @param ip l'adresse IP
     * @return nombre de tentatives restantes (0 si déjà bloqué)
     */
    public int getRemainingAttempts(String ip, String username) {
        Integer attempts = attemptsCache.getIfPresent(ip);
        Integer scopedAttempts = attemptsCache.getIfPresent(keyFor(ip, username));
        int ipAttempts = attempts == null ? 0 : attempts;
        int userAttempts = scopedAttempts == null ? 0 : scopedAttempts;
        int effectiveAttempts = Math.max(ipAttempts, userAttempts);
        return Math.max(0, maxAttempts - effectiveAttempts);
    }
}
