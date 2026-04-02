package com.lexdata.calendrier.controllers;

import com.lexdata.calendrier.models.CalendarEvent;
import com.lexdata.calendrier.models.UserCalendarConfig;
import com.lexdata.calendrier.repository.CalendarEventRepository;
import com.lexdata.calendrier.repository.UserCalendarConfigRepository;
import com.lexdata.calendrier.services.CalendrierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendrier")
public class CalendrierController {

    private final CalendrierService calendrierService;
    private final UserCalendarConfigRepository configRepository;
    private final CalendarEventRepository eventRepository;

    public CalendrierController(CalendrierService calendrierService, 
                                UserCalendarConfigRepository configRepository, 
                                CalendarEventRepository eventRepository) {
        this.calendrierService = calendrierService;
        this.configRepository = configRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/events")
    public List<CalendarEvent> getMyEvents() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return eventRepository.findByUsername(username);
    }

    @GetMapping("/config")
    public ResponseEntity<UserCalendarConfig> getConfig() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return configRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/config")
    public ResponseEntity<UserCalendarConfig> updateConfig(@RequestBody UserCalendarConfig config) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        config.setUsername(username);
        return ResponseEntity.ok(configRepository.save(config));
    }

    @PostMapping("/events/manuel")
    public ResponseEntity<CalendarEvent> createManualEvent(@RequestBody CalendarEvent event) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        event.setUsername(username);
        event.setManuel(true);
        event.setStatus(CalendarEvent.EventStatus.A_VENIR);
        return ResponseEntity.ok(eventRepository.save(event));
    }

    @GetMapping("/export/ical")
    public ResponseEntity<String> exportICal() {
        // En prod, générer un fichier .ics dynamique
        return ResponseEntity.ok("BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//LexData//Calendrier//FR\nEND:VCALENDAR");
    }
}
