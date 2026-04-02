package com.lexdata.juridique.repository;

import com.lexdata.juridique.models.LegalDomain;
import com.lexdata.juridique.models.TexteJuridique;
import com.lexdata.juridique.models.TypeTexte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TexteJuridiqueRepository extends JpaRepository<TexteJuridique, Long> {
        Optional<TexteJuridique> findByReferenceOfficielle(String referenceOfficielle);

        boolean existsByReferenceOfficielle(String referenceOfficielle);

        Page<TexteJuridique> findByTitreContainingIgnoreCase(String titre, Pageable pageable);

        Optional<TexteJuridique> findByIdAndEstPublieTrue(Long id);

        @Query("SELECT t FROM TexteJuridique t WHERE " +
                        "(:recherche IS NULL OR LOWER(t.titre) LIKE :recherche OR LOWER(t.contenu) LIKE :recherche) AND "
                        +
                        "(:domaine IS NULL OR t.domaine = :domaine) AND " +
                        "(:type IS NULL OR t.type = :type) AND t.estPublie = true")
        Page<TexteJuridique> searchAdvancedPublished(
                        @Param("recherche") String recherche,
                        @Param("domaine") LegalDomain domaine,
                        @Param("type") TypeTexte type,
                        Pageable pageable);

        @Query("SELECT t FROM TexteJuridique t WHERE " +
                        "(:recherche IS NULL OR LOWER(t.titre) LIKE :recherche OR LOWER(t.contenu) LIKE :recherche) AND "
                        +
                        "(:domaine IS NULL OR t.domaine = :domaine) AND " +
                        "(:type IS NULL OR t.type = :type)")
        Page<TexteJuridique> searchAdvancedAll(
                        @Param("recherche") String recherche,
                        @Param("domaine") LegalDomain domaine,
                        @Param("type") TypeTexte type,
                        Pageable pageable);
}
