// package com.lexdata.veille.controllers;

// import com.lexdata.veille.dto.AlerteVeilleDto;
// import com.lexdata.veille.models.AlerteVeille;
// import com.lexdata.veille.repository.AlerteVeilleRepository;
// import com.lexdata.veille.services.NotificationService;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.http.ResponseEntity;

// import java.util.Collections;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class VeilleControllerTest {

// @Mock
// private AlerteVeilleRepository alerteRepository;

// @Mock
// private NotificationService notificationService;

// @InjectMocks
// private VeilleController veilleController;

// @Test
// void getAllAlertes_ShouldReturnPage() {
// // Arrange
// Page<AlerteVeille> page = new PageImpl<>(Collections.emptyList());
// when(alerteRepository.findAll(any(PageRequest.class))).thenReturn(page);

// // Act
// Page<AlerteVeilleDto> result =
// veilleController.getAllAlertes(PageRequest.of(0, 10));

// // Assert
// assertNotNull(result);
// verify(alerteRepository).findAll(any(PageRequest.class));
// }

// @Test
// void updateStatus_ShouldUpdateAndNotify_WhenPublished() {
// // Arrange
// Long alerteId = 1L;
// AlerteVeille alerte = AlerteVeille.builder()
// .id(alerteId)
// .statut(AlerteVeille.WorkflowStatus.BROUILLON)
// .titre("Flash Info")
// .resumeClarifie("Résumé")
// .urgence(AlerteVeille.UrgenceLevel.MOYENNE)
// .build();

// when(alerteRepository.findById(alerteId)).thenReturn(Optional.of(alerte));
// when(alerteRepository.save(any(AlerteVeille.class))).thenAnswer(i ->
// i.getArgument(0));

// // Act
// ResponseEntity<AlerteVeilleDto> response =
// veilleController.updateStatus(alerteId,
// AlerteVeille.WorkflowStatus.PUBLIE);

// // Assert
// assertEquals(200, response.getStatusCode().value());
// verify(alerteRepository).save(argThat(a -> a.getStatut() ==
// AlerteVeille.WorkflowStatus.PUBLIE));
// verify(notificationService).dispatchAlerte(any(AlerteVeille.class));
// }
// }
