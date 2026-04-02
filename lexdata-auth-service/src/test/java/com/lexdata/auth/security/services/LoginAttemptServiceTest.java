package com.lexdata.auth.security.services;

import com.lexdata.auth.config.BruteForceProtectionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour LoginAttemptService.
 * Aucun contexte Spring nécessaire : tests rapides et isolés.
 */
@DisplayName("LoginAttemptService - Protection anti-brute-force")
class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USERNAME = "test_user";
    private static final int MAX_ATTEMPTS = 5;

    @BeforeEach
    void setUp() {
        BruteForceProtectionProperties properties = new BruteForceProtectionProperties();
        properties.setMaxAttempts(MAX_ATTEMPTS);
        properties.setLockDurationMinutes(15);
        loginAttemptService = new LoginAttemptService(properties);
    }

    @Test
    @DisplayName("Une IP inconnue ne doit pas être bloquée initialement")
    void shouldNotBeBlockedInitially() {
        assertThat(loginAttemptService.isBlocked(TEST_IP, TEST_USERNAME)).isFalse();
    }

    @Test
    @DisplayName("Le nombre de tentatives restantes doit être max pour une IP inconnue")
    void shouldReturnMaxAttemptsForUnknownIP() {
        assertThat(loginAttemptService.getRemainingAttempts(TEST_IP, TEST_USERNAME)).isEqualTo(MAX_ATTEMPTS);
    }

    @Test
    @DisplayName("L'IP ne doit pas être bloquée avant d'atteindre maxAttempts")
    void shouldNotBlockBeforeMaxAttempts() {
        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
            loginAttemptService.loginFailed(TEST_IP, TEST_USERNAME);
        }
        assertThat(loginAttemptService.isBlocked(TEST_IP, TEST_USERNAME)).isFalse();
        assertThat(loginAttemptService.getRemainingAttempts(TEST_IP, TEST_USERNAME)).isEqualTo(1);
    }

    @Test
    @DisplayName("L'IP doit être bloquée après exactement maxAttempts échecs consécutifs")
    void shouldBlockAfterMaxAttempts() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            loginAttemptService.loginFailed(TEST_IP, TEST_USERNAME);
        }
        assertThat(loginAttemptService.isBlocked(TEST_IP, TEST_USERNAME)).isTrue();
        assertThat(loginAttemptService.getRemainingAttempts(TEST_IP, TEST_USERNAME)).isEqualTo(0);
    }

    @Test
    @DisplayName("Un succès doit réinitialiser le compteur et débloquer l'IP")
    void shouldResetCounterOnSuccess() {
        // 4 échecs
        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
            loginAttemptService.loginFailed(TEST_IP, TEST_USERNAME);
        }
        assertThat(loginAttemptService.isBlocked(TEST_IP, TEST_USERNAME)).isFalse();

        // Connexion réussie
        loginAttemptService.loginSucceeded(TEST_IP, TEST_USERNAME);

        // Compteur réinitialisé
        assertThat(loginAttemptService.isBlocked(TEST_IP, TEST_USERNAME)).isFalse();
        assertThat(loginAttemptService.getRemainingAttempts(TEST_IP, TEST_USERNAME)).isEqualTo(MAX_ATTEMPTS);
    }

    @Test
    @DisplayName("Un succès doit réinitialiser même après un blocage complet")
    void shouldUnblockAfterSuccessEvenIfPreviouslyBlocked() {
        // Bloquer l'IP
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            loginAttemptService.loginFailed(TEST_IP, TEST_USERNAME);
        }
        assertThat(loginAttemptService.isBlocked(TEST_IP, TEST_USERNAME)).isTrue();

        // Simuler un succès (ex: admin resetant les tentatives)
        loginAttemptService.loginSucceeded(TEST_IP, TEST_USERNAME);
        assertThat(loginAttemptService.isBlocked(TEST_IP, TEST_USERNAME)).isFalse();
    }

    @Test
    @DisplayName("Deux IPs différentes doivent être traitées indépendamment")
    void shouldTrackDifferentIPsIndependently() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            loginAttemptService.loginFailed(ip1, TEST_USERNAME);
        }

        assertThat(loginAttemptService.isBlocked(ip1, TEST_USERNAME)).isTrue();
        assertThat(loginAttemptService.isBlocked(ip2, TEST_USERNAME)).isFalse();
    }
}
