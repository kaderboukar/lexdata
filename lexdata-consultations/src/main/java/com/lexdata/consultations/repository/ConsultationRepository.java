package com.lexdata.consultations.repository;

import com.lexdata.consultations.models.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByClientUsername(String username);
    List<Consultation> findByJuristeUsername(String username);
}
