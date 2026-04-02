package com.lexdata.juridique.repository;

import com.lexdata.juridique.models.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndTexteId(String userId, Long texteId);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(String userId);
}
