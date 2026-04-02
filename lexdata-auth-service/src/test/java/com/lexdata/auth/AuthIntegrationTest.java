// package com.lexdata.auth;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.lexdata.auth.models.ERole;
// import com.lexdata.auth.models.RefreshToken;
// import com.lexdata.auth.models.Role;
// import com.lexdata.auth.models.User;
// import com.lexdata.auth.payload.request.LoginRequest;
// import com.lexdata.auth.payload.request.SignupRequest;
// import com.lexdata.auth.repository.RefreshTokenRepository;
// import com.lexdata.auth.repository.RoleRepository;
// import com.lexdata.auth.repository.UserRepository;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.Collections;
// import java.util.Optional;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// public class AuthIntegrationTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private UserRepository userRepository;

//     @MockBean
//     private RoleRepository roleRepository;

//     @MockBean
//     private RefreshTokenRepository refreshTokenRepository;

//     @Test
//     void registerAndLoginFlow_ShouldSucceed() throws Exception {
//         // Prepare Roles
//         Role userRole = new Role();
//         userRole.setName(ERole.ROLE_USER);
//         when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
//         when(userRepository.existsByUsername(anyString())).thenReturn(false);
//         when(userRepository.existsByEmail(anyString())).thenReturn(false);

//         // 1. Register
//         SignupRequest signupRequest = new SignupRequest();
//         signupRequest.setUsername("testuser_it");
//         signupRequest.setEmail("test_it@example.com");
//         signupRequest.setPassword("password123");
//         signupRequest.setFirstName("First");
//         signupRequest.setLastName("Last");
//         signupRequest.setTelephone("12345678");
//         signupRequest.setRole(Collections.singleton("user"));

//         mockMvc.perform(post("/api/auth/register")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(signupRequest)))
//                 .andExpect(status().isOk());

//         // 2. Mock for Login
//         User user = new User();
//         user.setId(1L);
//         user.setUsername("testuser_it");
//         user.setEmail("test_it@example.com");
//         user.setPassword(new BCryptPasswordEncoder().encode("password123"));
//         user.setRoles(Collections.singleton(userRole));
//         user.setActive(true);

//         when(userRepository.findByUsername("testuser_it")).thenReturn(Optional.of(user));
//         when(userRepository.findById(1L)).thenReturn(Optional.of(user));

//         RefreshToken refreshToken = new RefreshToken();
//         refreshToken.setToken("dummy-refresh-token");
//         refreshToken.setUser(user);
//         when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

//         // 2. Login
//         LoginRequest loginRequest = new LoginRequest();
//         loginRequest.setUsername("testuser_it");
//         loginRequest.setPassword("password123");

//         mockMvc.perform(post("/api/auth/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").exists())
//                 .andExpect(jsonPath("$.username").value("testuser_it"));
//     }
// }
