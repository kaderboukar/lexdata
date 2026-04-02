package com.lexdata.user.controllers;

import com.lexdata.user.dto.KycVerificationRequest;
import com.lexdata.user.dto.UserProfileDto;
import com.lexdata.user.models.UserProfile;
import com.lexdata.user.models.VerificationStatus;
import com.lexdata.user.repository.UserProfileRepository;
import com.lexdata.user.services.SecurityAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileControllerTest {

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private SecurityAuditService securityAuditService;

    private UserProfileController userProfileController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("adminUser", null)
        );
        userProfileController = new UserProfileController(profileRepository, securityAuditService);
    }

    @Test
    void getProfileByUsername_ShouldReturnProfile_WhenExists() {
        // Arrange
        UserProfile profile = UserProfile.builder()
                .username("testuser")
                .fullName("Test User")
                .build();
        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(profile));

        // Act
        ResponseEntity<UserProfileDto> response = userProfileController.getProfileByUsername("testuser");

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(profileRepository).findByUsername("testuser");
    }

    @Test
    void getProfileByUsername_ShouldReturnNotFound_WhenNotExists() {
        // Arrange
        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserProfileDto> response = userProfileController.getProfileByUsername("testuser");

        // Assert
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void verifyExpertProfile_ShouldUpdateStatus_Save_AndAuditLog() {
        // Arrange
        KycVerificationRequest request = new KycVerificationRequest();
        request.setStatus(VerificationStatus.VERIFIED);

        UserProfile existingProfile = UserProfile.builder()
                .id(10L)
                .username("testuser")
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ResponseEntity<UserProfileDto> response = userProfileController.verifyExpertProfile("testuser", request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("VERIFIED", response.getBody().getVerificationStatus());
        verify(profileRepository).save(any(UserProfile.class));
        verify(securityAuditService).log(eq("USER_PROFILE_VERIFIED_BY_ADMIN"), eq(10L), eq("adminUser"), contains("targetUsername=testuser"));
    }
}
