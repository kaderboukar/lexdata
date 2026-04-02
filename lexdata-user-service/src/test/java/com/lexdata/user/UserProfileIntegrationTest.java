package com.lexdata.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexdata.user.dto.KycVerificationRequest;
import com.lexdata.user.models.UserProfile;
import com.lexdata.user.models.VerificationStatus;
import com.lexdata.user.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileRepository profileRepository;

    @Test
    @WithMockUser(username = "admin", roles = { "AGENT_ADMIN" })
    void getProfileByUsername_WhenExists_ShouldReturnProfile() throws Exception {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .username("testuser")
                .fullName("Test User")
                .city("Niamey")
                .build();

        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(profile));

        // Act & Assert
        mockMvc.perform(get("/api/profiles/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "AGENT_ADMIN" })
    void verifyExpertProfile_ShouldUpdateStatus() throws Exception {
        // Arrange
        KycVerificationRequest request = new KycVerificationRequest();
        request.setStatus(VerificationStatus.VERIFIED);

        UserProfile existing = UserProfile.builder()
                .id(10L)
                .username("testuser")
                .fullName("Test User")
                .city("Niamey")
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        UserProfile saved = UserProfile.builder()
                .id(10L)
                .username("testuser")
                .fullName("Test User")
                .city("Niamey")
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(saved);

        mockMvc.perform(put("/api/profiles/testuser/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }
}
