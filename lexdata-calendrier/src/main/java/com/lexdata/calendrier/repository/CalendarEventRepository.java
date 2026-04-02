package com.lexdata.calendrier.repository;

import com.lexdata.calendrier.models.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUsername(String username);
    List<CalendarEvent> findByUsernameAndStatus(String username, CalendarEvent.EventStatus status);
}
