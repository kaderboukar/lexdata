package com.lexdata.consultations.repository;

import com.lexdata.consultations.models.CompanyCreationPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyCreationPackRepository extends JpaRepository<CompanyCreationPack, Long> {
    List<CompanyCreationPack> findByOwnerUsername(String username);
}
