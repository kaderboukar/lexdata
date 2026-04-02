package com.lexdata.auth.repository;

import com.lexdata.auth.models.TokenType;
import com.lexdata.auth.models.User;
import com.lexdata.auth.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Retrouve un token par sa valeur et son type.
     * Utilisé pour valider un reset ou une vérification email.
     */
    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);

    /**
     * Supprime les tokens existants d'un type donné pour un utilisateur.
     * Garantit qu'un seul token actif par user/type existe à la fois.
     */
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user AND vt.type = :type")
    void deleteByUserAndType(@Param("user") User user, @Param("type") TokenType type);

    /**
     * Nettoyage périodique des tokens expirés (appelable via @Scheduled si besoin).
     */
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < :now OR vt.used = true")
    void deleteExpiredAndUsedTokens(@Param("now") LocalDateTime now);
}
