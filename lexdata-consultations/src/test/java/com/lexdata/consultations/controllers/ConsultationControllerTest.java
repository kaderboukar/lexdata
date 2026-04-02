package com.lexdata.consultations.controllers;

import com.lexdata.consultations.models.Consultation;
import com.lexdata.consultations.models.CompanyCreationPack;
import com.lexdata.consultations.repository.CompanyCreationPackRepository;
import com.lexdata.consultations.repository.ConsultationRepository;
import com.lexdata.consultations.services.ConsultationService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsultationControllerTest {

    @Mock
    private ConsultationService consultationService;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private CompanyCreationPackRepository packRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ConsultationController consultationController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test_user");
    }

    @Test
    void getMyConsultations_ShouldReturnList() {
        // Arrange
        when(consultationRepository.findByClientUsername("test_user")).thenReturn(Collections.emptyList());

        // Act
        List<Consultation> result = consultationController.getMyConsultations();

        // Assert
        assertNotNull(result);
        verify(consultationRepository).findByClientUsername("test_user");
    }

    @Test
    void bookConsultation_ShouldSetUsernameAndCallService() {
        // Arrange
        Consultation consultation = new Consultation();
        consultation.setTitre("Besoin d'aide");
        when(consultationService.reserverConsultation(any(Consultation.class))).thenReturn(consultation);

        // Act
        ResponseEntity<Consultation> response = consultationController.bookConsultation(consultation);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("test_user", response.getBody().getClientUsername());
        verify(consultationService).reserverConsultation(consultation);
    }

    @Test
    void getMyPacks_ShouldReturnList() {
        // Arrange
        when(packRepository.findByOwnerUsername("test_user")).thenReturn(Collections.emptyList());

        // Act
        List<CompanyCreationPack> result = consultationController.getMyPacks();

        // Assert
        assertNotNull(result);
        verify(packRepository).findByOwnerUsername("test_user");
    }
}
