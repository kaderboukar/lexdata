package com.lexdata.synthese.controllers;

import com.lexdata.synthese.models.FicheSynthetique;
import com.lexdata.synthese.repository.FicheSynthetiqueRepository;
import com.lexdata.synthese.repository.SyntheseVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SyntheseControllerTest {

    @Mock
    private FicheSynthetiqueRepository ficheRepository;

    @Mock
    private SyntheseVersionRepository versionRepository;

    @Mock
    private com.lexdata.synthese.services.ExportService exportService;

    @Mock
    private com.lexdata.synthese.services.LegalTextValidationService legalTextValidationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SyntheseController syntheseController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getPublishedFiches_ShouldReturnPage() {
        // Arrange
        Page<FicheSynthetique> page = new PageImpl<>(Collections.emptyList());
        when(ficheRepository.findByStatus(eq(FicheSynthetique.SyntheseStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<?> result = syntheseController.getPublishedFiches(PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        verify(ficheRepository).findByStatus(eq(FicheSynthetique.SyntheseStatus.PUBLISHED), any(Pageable.class));
    }

    @Test
    void updateStatus_ShouldUpdateAndSave() {
        // Arrange
        Long ficheId = 1L;
        FicheSynthetique fiche = FicheSynthetique.builder()
                .id(ficheId)
                .status(FicheSynthetique.SyntheseStatus.DRAFT)
                .build();

        when(ficheRepository.findById(ficheId)).thenReturn(Optional.of(fiche));
        when(ficheRepository.save(any(FicheSynthetique.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ResponseEntity<?> response = syntheseController.updateStatus(ficheId, FicheSynthetique.SyntheseStatus.PUBLISHED);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(ficheRepository).save(argThat(f -> f.getStatus() == FicheSynthetique.SyntheseStatus.PUBLISHED));
    }
}
