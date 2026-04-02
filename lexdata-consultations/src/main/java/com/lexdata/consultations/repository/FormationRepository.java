package com.lexdata.consultations.repository;

import com.lexdata.consultations.models.Formation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {
}
