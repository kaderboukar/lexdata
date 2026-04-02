package com.lexdata.auth;

import com.lexdata.auth.payload.request.SignupRequest;
import com.lexdata.auth.repository.UserRepository;
import com.lexdata.auth.repository.RoleRepository;
import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.Role;
import com.lexdata.auth.security.services.UserEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FullUserFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserEventPublisher userEventPublisher;

    @BeforeAll
    void setup() {
        // Initialiser les rôles si nécessaire
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(ERole.ROLE_USER);
            roleRepository.save(userRole);
        }
    }

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    @Test
    void registerUser_ShouldInvokeUserEventPublisher_AndReturnSuccess() throws Exception {
        // GIVEN
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("mock_e2e_user");
        signupRequest.setEmail("mock_e2e@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setTelephone("+22700000000");

        // WHEN
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                // THEN
                .andExpect(status().isOk());

        // Verify Event Publisher was called
        verify(userEventPublisher).publishUserRegisteredEvent(any());

        Assertions.assertTrue(userRepository.existsByUsername("mock_e2e_user"));
    }
}
