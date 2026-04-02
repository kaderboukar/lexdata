package com.lexdata.user.repository;

import com.lexdata.user.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // Permet de trouver un profil grâce au pseudo stocké dans le JWT
    Optional<UserProfile> findByUsername(String username);
    
    // Vérification d'existence
    Boolean existsByUsername(String username);
}