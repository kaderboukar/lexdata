package com.lexdata.monitoring.repository;

import com.lexdata.monitoring.models.TechnicalLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicalLogRepository extends JpaRepository<TechnicalLog, Long> {
    List<TechnicalLog> findByServiceNameOrderByTimestampDesc(String serviceName, Pageable pageable);
    List<TechnicalLog> findByLevelOrderByTimestampDesc(String level, Pageable pageable);
}
