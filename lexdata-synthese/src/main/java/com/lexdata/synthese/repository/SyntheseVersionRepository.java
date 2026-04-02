package com.lexdata.synthese.repository;

import com.lexdata.synthese.models.SyntheseVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyntheseVersionRepository extends JpaRepository<SyntheseVersion, Long> {
    List<SyntheseVersion> findByFicheIdOrderByDateVersionDesc(Long ficheId);

    @org.springframework.data.jpa.repository.Query("SELECT sv FROM SyntheseVersion sv WHERE sv.fiche.texteJuridiqueId = :texteId AND sv.texteJuridiqueVersionId = :versionTexte")
    List<SyntheseVersion> findByTexteIdAndVersion(Long texteId, Long versionTexte);
}
