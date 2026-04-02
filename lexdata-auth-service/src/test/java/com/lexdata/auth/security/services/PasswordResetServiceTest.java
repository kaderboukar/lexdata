package com.lexdata.auth.security.services;

import com.lexdata.auth.config.BruteForceProtectionProperties;
import com.lexdata.auth.models.TokenType;
import com.lexdata.auth.models.User;
import com.lexdata.auth.models.VerificationToken;
import com.lexdata.auth.repository.UserRepository;
import com.lexdata.auth.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour PasswordResetService.
 * Utilise Mockito pour isoler le service de ses dépendances.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService - Réinitialisation de mot de passe")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Injecter la valeur @Value manuellement
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");

        testUser = new User();
        testUser.setUsername("johndoe");
        testUser.setEmail("john@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPassword("hashedPassword");
    }

    @Test
    @DisplayName("Initiation reset : si l'email existe, génère et envoie un token")
    void shouldGenerateAndSendTokenWhenEmailExists() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        passwordResetService.initiatePasswordReset("john@example.com");

        verify(tokenRepository).deleteByUserAndType(eq(testUser), eq(TokenType.PASSWORD_RESET));
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendPasswordResetEmail(eq(testUser), any(String.class));
    }

    @Test
    @DisplayName("Initiation reset : si l'email n'existe pas, aucune action (anti-enumeration)")
    void shouldDoNothingWhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        passwordResetService.initiatePasswordReset("unknown@example.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    @DisplayName("Reset : token invalide → lève IllegalArgumentException")
    void shouldThrowWhenTokenNotFound() {
        when(tokenRepository.findByTokenAndType("bad-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword("bad-token", "newPass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalide");
    }

    @Test
    @DisplayName("Reset : token déjà utilisé → lève IllegalArgumentException")
    void shouldThrowWhenTokenAlreadyUsed() {
        VerificationToken token = new VerificationToken(
                "used-token", TokenType.PASSWORD_RESET,
                LocalDateTime.now().plusMinutes(10), testUser);
        token.setUsed(true);

        when(tokenRepository.findByTokenAndType("used-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword("used-token", "newPass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà été utilisé");
    }

    @Test
    @DisplayName("Reset : token expiré → lève IllegalArgumentException")
    void shouldThrowWhenTokenExpired() {
        VerificationToken token = new VerificationToken(
                "expired-token", TokenType.PASSWORD_RESET,
                LocalDateTime.now().minusMinutes(5), testUser); // expiré !

        when(tokenRepository.findByTokenAndType("expired-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword("expired-token", "newPass123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiré");
    }

    @Test
    @DisplayName("Reset valide → met à jour le mot de passe et invalide le token")
    void shouldResetPasswordSuccessfully() {
        VerificationToken token = new VerificationToken(
                "valid-token", TokenType.PASSWORD_RESET,
                LocalDateTime.now().plusMinutes(10), testUser);

        when(tokenRepository.findByTokenAndType("valid-token", TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass123")).thenReturn("newHashedPassword");

        passwordResetService.resetPassword("valid-token", "newPass123");

        assertThat(testUser.getPassword()).isEqualTo("newHashedPassword");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(testUser);
        verify(tokenRepository).save(token);
    }
}
