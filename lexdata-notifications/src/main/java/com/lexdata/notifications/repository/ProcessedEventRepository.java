package com.lexdata.notifications.repository;

import com.lexdata.notifications.models.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    Optional<ProcessedEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}
