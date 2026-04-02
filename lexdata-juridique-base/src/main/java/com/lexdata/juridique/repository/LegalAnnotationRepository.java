package com.lexdata.juridique.repository;

import com.lexdata.juridique.models.LegalAnnotation;
import com.lexdata.juridique.models.TexteJuridique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegalAnnotationRepository extends JpaRepository<LegalAnnotation, Long> {

    // Pour récupérer les notes d'un utilisateur sur un texte précis
    List<LegalAnnotation> findByUserIdAndTexte(String userId, TexteJuridique texte);

    // NOUVEAU : Pour récupérer TOUTES les notes d'un utilisateur, triées par date (récentes en premier)
    List<LegalAnnotation> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<LegalAnnotation> findByIdAndUserId(Long id, String userId);
}
