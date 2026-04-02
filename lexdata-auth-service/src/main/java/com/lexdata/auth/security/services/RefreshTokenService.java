package com.lexdata.auth.security.services;

import com.lexdata.auth.exceptions.RefreshTokenExpiredException;
import com.lexdata.auth.models.RefreshToken;
import com.lexdata.auth.models.User;
import com.lexdata.auth.repository.RefreshTokenRepository;
import com.lexdata.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${lexdata.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Erreur: Utilisateur introuvable"));

        // On cherche si un token existe déjà pour cet utilisateur
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);

        RefreshToken refreshToken;

        if (existingToken.isPresent()) {
            // S'il existe, on le récupère (Hibernate fera un UPDATE)
            refreshToken = existingToken.get();
        } else {
            // S'il n'existe pas, on en crée un nouveau (Hibernate fera un INSERT)
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
        }

        // On applique les nouvelles valeurs
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException("Refresh token expiré. Veuillez vous reconnecter.");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Erreur: Utilisateur introuvable avec l'id: " + userId));
        return refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public int deleteByToken(String token) {
        return refreshTokenRepository.deleteByToken(token);
    }
}
