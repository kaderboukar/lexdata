// package com.lexdata.juridique;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.lexdata.juridique.events.JuridiqueEventPublisher;
// import com.lexdata.juridique.models.LegalDomain;
// import com.lexdata.juridique.models.TexteJuridique;
// import com.lexdata.juridique.models.TypeTexte;
// import com.lexdata.juridique.repository.TexteJuridiqueRepository;
// import com.lexdata.juridique.services.TexteSearchService;

// import org.junit.jupiter.api.Test;

// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;

// import org.springframework.http.MediaType;

// import org.springframework.security.test.context.support.WithMockUser;

// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.Optional;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;

// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// class LegalTextIntegrationTest {

// @Autowired
// private MockMvc mockMvc;

// @Autowired
// private ObjectMapper objectMapper;

// @MockBean
// private TexteJuridiqueRepository texteRepository;

// // ✅ IMPORTANT : éviter les NullPointerException
// @MockBean
// private JuridiqueEventPublisher eventPublisher;

// @MockBean
// private TexteSearchService searchService;

// @Test
// @WithMockUser(username = "admin", roles = { "AGENT_ADMIN" })
// void createAndPublishText_ShouldSucceed() throws Exception {

// Map<String, Object> request = new HashMap<>();
// request.put("titre", "Loi de Finance 2026");
// request.put("referenceOfficielle", "LF-2026-001");
// request.put("type", "LOI");
// request.put("domaine", "DROIT_FISCAL");
// request.put("dateSignature", "2026-01-01");
// request.put("contenu", "Le contenu de la loi...");
// request.put("estPremium", true);

// TexteJuridique savedTexte = TexteJuridique.builder()
// .id(1L)
// .titre("Loi de Finance 2026")
// .referenceOfficielle("LF-2026-001")
// .type(TypeTexte.LOI)
// .domaine(LegalDomain.DROIT_FISCAL)
// .statut(TexteJuridique.WorkflowStatus.BROUILLON)
// .build();

// when(texteRepository.existsByReferenceOfficielle("LF-2026-001")).thenReturn(false);
// when(texteRepository.save(any(TexteJuridique.class))).thenReturn(savedTexte);

// mockMvc.perform(post("/api/juridique/textes")
// .contentType(MediaType.APPLICATION_JSON)
// .content(objectMapper.writeValueAsString(request)))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.titre").value("Loi de Finance 2026"))
// .andExpect(jsonPath("$.statut").value("BROUILLON"));
// }

// @Test
// @WithMockUser
// void getTexteById_WhenExists_ShouldReturnDto() throws Exception {

// TexteJuridique texte = TexteJuridique.builder()
// .id(1L)
// .titre("Code du Travail")
// .referenceOfficielle("CT-2025")
// .type(TypeTexte.LOI)
// .domaine(LegalDomain.DROIT_TRAVAIL)
// .statut(TexteJuridique.WorkflowStatus.PUBLIE)
// .estPublie(true)
// .build();

// when(texteRepository.findById(1L)).thenReturn(Optional.of(texte));

// mockMvc.perform(get("/api/juridique/textes/1"))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.titre").value("Code du Travail"))
// .andExpect(jsonPath("$.estPublie").value(true));
// }
// }