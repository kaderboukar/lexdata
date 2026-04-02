package com.lexdata.calendrier.controllers;

import com.lexdata.calendrier.models.CalendarEvent;
import com.lexdata.calendrier.models.UserCalendarConfig;
import com.lexdata.calendrier.repository.CalendarEventRepository;
import com.lexdata.calendrier.repository.UserCalendarConfigRepository;
import com.lexdata.calendrier.services.CalendrierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CalendrierControllerTest {

    @Mock
    private CalendrierService calendrierService;

    @Mock
    private UserCalendarConfigRepository configRepository;

    @Mock
    private CalendarEventRepository eventRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CalendrierController calendrierController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test_user");
    }

    @Test
    void getMyEvents_ShouldReturnList() {
        // Arrange
        when(eventRepository.findByUsername("test_user")).thenReturn(Collections.emptyList());

        // Act
        List<CalendarEvent> result = calendrierController.getMyEvents();

        // Assert
        assertNotNull(result);
        verify(eventRepository).findByUsername("test_user");
    }

    @Test
    void createManualEvent_ShouldSetUsernameAndSave() {
        // Arrange
        CalendarEvent event = new CalendarEvent();
        event.setTitre("Mon Event");
        when(eventRepository.save(any(CalendarEvent.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ResponseEntity<CalendarEvent> response = calendrierController.createManualEvent(event);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("test_user", response.getBody().getUsername());
        assertTrue(response.getBody().isManuel());
        assertEquals(CalendarEvent.EventStatus.A_VENIR, response.getBody().getStatus());
        verify(eventRepository).save(event);
    }
}
