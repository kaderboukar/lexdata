package com.lexdata.veille.repository;

import com.lexdata.veille.models.AlerteVeille;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface AlerteVeilleRepository extends JpaRepository<AlerteVeille, Long> {

    @Query("SELECT DISTINCT a FROM AlerteVeille a "
            + "LEFT JOIN FETCH a.domainesCibles "
            + "LEFT JOIN FETCH a.secteursImpactes "
            + "WHERE a.id = :id")
    Optional<AlerteVeille> findByIdWithCollections(@Param("id") Long id);

    @Query("SELECT a FROM AlerteVeille a JOIN a.domainesCibles d WHERE " +
           "a.status = 'PUBLISHED' AND " +
           "(:domaines IS NULL OR d IN :domaines)")
    Page<AlerteVeille> findPublishedByDomaines(@Param("domaines") Set<String> domaines, Pageable pageable);

    Page<AlerteVeille> findByStatus(AlerteVeille.AlertStatus status, Pageable pageable);

    boolean existsByTexteJuridiqueIdAndEventTypeAndStatus(
            Long texteJuridiqueId,
            AlerteVeille.EventType eventType,
            AlerteVeille.AlertStatus status
    );
}
