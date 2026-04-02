package com.lexdata.annuaire.controllers;

import com.lexdata.annuaire.models.ProfessionalProfile;
import com.lexdata.annuaire.repository.LeadRepository;
import com.lexdata.annuaire.repository.ProfessionalProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnnuaireControllerTest {

    @Mock
    private ProfessionalProfileRepository profileRepository;

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private AnnuaireController annuaireController;

    @Test
    void searchProfiles_ShouldReturnPage() {
        // Arrange
        Page<ProfessionalProfile> page = new PageImpl<>(Collections.emptyList());
        when(profileRepository.searchProfiles(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<?> result = annuaireController.search(null, null, mock(Pageable.class));

        // Assert
        assertNotNull(result);
        verify(profileRepository).searchProfiles(any(), any(), any(Pageable.class));
    }

    @Test
    void validateProfile_ShouldUpdateStatus() {
        // Arrange
        Long profileId = 1L;
        ProfessionalProfile profile = ProfessionalProfile.builder()
                .id(profileId)
                .username("maitre_test")
                .statut(ProfessionalProfile.ValidationStatus.EN_ATTENTE)
                .build();

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(ProfessionalProfile.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ResponseEntity<?> response = annuaireController.validateProfile(profileId,
                ProfessionalProfile.ValidationStatus.VALIDE);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(profileRepository).save(argThat(p -> p.getStatut() == ProfessionalProfile.ValidationStatus.VALIDE));
    }
}
