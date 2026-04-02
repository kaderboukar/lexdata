package com.lexdata.auth.repository;

import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true) // OPTIMISATION CRITIQUE : Accélère toutes les lectures (Login)
public interface UserRepository extends JpaRepository<User, Long> {

    // Utilisé pour le Login : Hibernate ajoute automatiquement "AND is_deleted = false"
    // grâce à l'annotation sur l'Entité.
    Optional<User> findByUsername(String username);

    // Utilisé pour vérifier l'unicité lors de l'inscription (Fail-Fast)
    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    // Pour l'envoi de mail ou récupération de mot de passe
    Optional<User> findByEmail(String email);

    // --- ZONE ADMINISTRATIVE (Bypass du Soft Delete) ---
    // Cette méthode permet à un Super Admin de retrouver un utilisateur 
    // même s'il a été "supprimé" (soft deleted), pour le restaurer.
    // On utilise nativeQuery = true pour parler directement au SQL et ignorer Hibernate.
    @Query(value = "SELECT * FROM users WHERE email = :email AND is_deleted = true", nativeQuery = true)
    Optional<User> findDeletedUserByEmail(@Param("email") String email);

    // NOUVELLE MÉTHODE POUR LA RECHERCHE ADMIN
    @Query("SELECT u FROM User u WHERE "
            + "(:search IS NULL OR :search = '' OR "
            + "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR "
            + "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
            + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
            + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    @Query("SELECT u.id FROM User u")
    Page<Long> findAllUserIds(Pageable pageable);

    @Query("SELECT u.id FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<Long> findUserIdsByRole(@Param("roleName") ERole roleName, Pageable pageable);

    @Query("SELECT u.id FROM User u WHERE u.username IN :usernames")
    List<Long> findIdsByUsernamesIn(@Param("usernames") Collection<String> usernames);

}
