package com.lexdata.calendrier.repository;

import com.lexdata.calendrier.models.LegalObligationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalObligationRuleRepository extends JpaRepository<LegalObligationRule, Long> {
}
