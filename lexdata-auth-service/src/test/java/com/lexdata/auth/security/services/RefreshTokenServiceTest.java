package com.lexdata.auth.security.services;

import com.lexdata.auth.exceptions.RefreshTokenExpiredException;
import com.lexdata.auth.models.RefreshToken;
import com.lexdata.auth.models.User;
import com.lexdata.auth.repository.RefreshTokenRepository;
import com.lexdata.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 3600000L);
    }

    @Test
    void createRefreshToken_ShouldReturnToken() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        // Assert
        assertNotNull(refreshToken);
        assertEquals(user, refreshToken.getUser());
        assertNotNull(refreshToken.getToken());
        assertNotNull(refreshToken.getExpiryDate());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_ShouldThrowException_WhenTokenIsExpired() {
        // Arrange
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(10));

        // Act & Assert
        RefreshTokenExpiredException exception = assertThrows(RefreshTokenExpiredException.class,
                () -> refreshTokenService.verifyExpiration(token));
        assertTrue(exception.getMessage().toLowerCase().contains("expir"));
        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void verifyExpiration_ShouldReturnToken_WhenNotExpired() {
        // Arrange
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusSeconds(10));

        // Act
        RefreshToken result = refreshTokenService.verifyExpiration(token);

        // Assert
        assertEquals(token, result);
        verify(refreshTokenRepository, never()).delete(any());
    }
}
