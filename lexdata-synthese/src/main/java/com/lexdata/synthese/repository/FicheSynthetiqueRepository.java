package com.lexdata.synthese.repository;

import com.lexdata.synthese.models.FicheSynthetique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FicheSynthetiqueRepository extends JpaRepository<FicheSynthetique, Long> {
    java.util.List<FicheSynthetique> findByTexteJuridiqueId(Long texteJuridiqueId);

    Page<FicheSynthetique> findByStatus(FicheSynthetique.SyntheseStatus status, Pageable pageable);

    java.util.List<FicheSynthetique> findByTexteJuridiqueIdAndStatusOrderByVersionDesc(
            Long texteJuridiqueId,
            FicheSynthetique.SyntheseStatus status
    );

    @org.springframework.data.jpa.repository.Query("""
            SELECT f FROM FicheSynthetique f
            WHERE f.texteJuridiqueId = :texteJuridiqueId AND f.status = 'PUBLISHED'
            ORDER BY f.version DESC
            """)
    java.util.List<FicheSynthetique> findPublishedByTexteJuridiqueId(
            @org.springframework.data.repository.query.Param("texteJuridiqueId") Long texteJuridiqueId);
}
