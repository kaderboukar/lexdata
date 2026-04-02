package com.lexdata.annuaire.repository;

import com.lexdata.annuaire.models.ProfessionalProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, Long> {

    Optional<ProfessionalProfile> findByUsername(String username);

    @Query("SELECT p FROM ProfessionalProfile p " +
           "WHERE p.statut = 'VALIDE' " +
           "AND (:expertise IS NULL OR :expertise MEMBER OF p.expertises) " +
           "AND (:ville IS NULL OR LOWER(p.ville) = LOWER(:ville))")
    Page<ProfessionalProfile> searchProfiles(@Param("expertise") String expertise, 
                                            @Param("ville") String ville, 
                                            Pageable pageable);

    Page<ProfessionalProfile> findByStatut(ProfessionalProfile.ValidationStatus statut, Pageable pageable);
}
