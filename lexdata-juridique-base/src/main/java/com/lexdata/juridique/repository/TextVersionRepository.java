package com.lexdata.juridique.repository;

import com.lexdata.juridique.models.TextVersion;
import com.lexdata.juridique.models.TexteJuridique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextVersionRepository extends JpaRepository<TextVersion, Long> {
    List<TextVersion> findByTexteOrderByDateVersionDesc(TexteJuridique texte);
}
