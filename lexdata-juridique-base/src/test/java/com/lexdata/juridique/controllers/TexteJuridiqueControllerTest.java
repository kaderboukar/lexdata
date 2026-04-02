// package com.lexdata.juridique.controllers;

// import com.lexdata.juridique.events.JuridiqueEventPublisher;
// import com.lexdata.juridique.models.LegalDomain;
// import com.lexdata.juridique.models.TexteJuridique;
// import com.lexdata.juridique.models.TypeTexte;
// import com.lexdata.juridique.repository.LegalAnnotationRepository;
// import com.lexdata.juridique.repository.TextVersionRepository;
// import com.lexdata.juridique.repository.TexteJuridiqueRepository;
// import com.lexdata.juridique.services.TexteSearchService;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.http.ResponseEntity;

// import java.util.Collections;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class TexteJuridiqueControllerTest {

// @Mock
// private TexteJuridiqueRepository texteRepository;

// @Mock
// private TextVersionRepository versionRepository;

// @Mock
// private LegalAnnotationRepository annotationRepository;

// // ✅ AJOUT IMPORTANT
// @Mock
// private JuridiqueEventPublisher eventPublisher;

// @Mock
// private TexteSearchService searchService;

// @InjectMocks
// private TexteJuridiqueController texteJuridiqueController;

// @Test
// void searchTextes_ShouldReturnPage() {

// Page<TexteJuridique> page = new PageImpl<>(Collections.emptyList());

// when(texteRepository.searchAdvanced(any(), any(), any(), anyBoolean(),
// any(Pageable.class)))
// .thenReturn(page);

// Page<?> result = texteJuridiqueController.searchTextes(
// null, null, null, false, PageRequest.of(0, 10));

// assertNotNull(result);

// verify(texteRepository)
// .searchAdvanced(any(), any(), any(), anyBoolean(), any(Pageable.class));
// }

// @Test
// void getTexteById_ShouldReturnTexte_WhenExists() {

// TexteJuridique texte = TexteJuridique.builder()
// .id(1L)
// .titre("Loi Test")
// .type(TypeTexte.LOI)
// .domaine(LegalDomain.DROIT_FISCAL)
// .statut(TexteJuridique.WorkflowStatus.PUBLIE)
// .build();

// when(texteRepository.findById(1L)).thenReturn(Optional.of(texte));

// ResponseEntity<?> response = texteJuridiqueController.getTexteById(1L);

// assertEquals(200, response.getStatusCode().value());

// verify(texteRepository).findById(1L);
// }

// @Test
// void updateStatus_ShouldUpdateAndSave() {

// TexteJuridique texte = TexteJuridique.builder()
// .id(1L)
// .type(TypeTexte.LOI)
// .domaine(LegalDomain.AUTRE)
// .statut(TexteJuridique.WorkflowStatus.BROUILLON)
// .build();

// when(texteRepository.findById(1L)).thenReturn(Optional.of(texte));

// when(texteRepository.save(any(TexteJuridique.class)))
// .thenAnswer(i -> i.getArgument(0));

// // Mock pour éviter les effets de bord
// doNothing().when(eventPublisher).publishTextePublie(any());

// doNothing().when(searchService).indexTexte(any());

// ResponseEntity<?> response = texteJuridiqueController.updateStatus(
// 1L,
// TexteJuridique.WorkflowStatus.PUBLIE);

// assertEquals(200, response.getStatusCode().value());

// verify(texteRepository).save(
// argThat(t -> t.getStatut() == TexteJuridique.WorkflowStatus.PUBLIE
// && t.getEstPublie()));

// // Vérifie que l'événement est bien publié
// verify(eventPublisher).publishTextePublie(any(TexteJuridique.class));
// }
// }