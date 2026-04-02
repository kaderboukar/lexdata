package com.lexdata.user.controllers;

import com.lexdata.user.dto.KycVerificationRequest;
import com.lexdata.user.dto.UserProfileDto;
import com.lexdata.user.models.UserProfile;
import com.lexdata.user.repository.UserProfileRepository;
import com.lexdata.user.services.SecurityAuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    private final UserProfileRepository profileRepository;
    private final SecurityAuditService securityAuditService;

    public UserProfileController(UserProfileRepository profileRepository, SecurityAuditService securityAuditService) {
        this.profileRepository = profileRepository;
        this.securityAuditService = securityAuditService;
    }

    // ADMIN: Lister tous les profils (Paginated)
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserProfileDto>> getAllProfiles(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserProfileDto> profiles = profileRepository.findAll(pageable).map(this::mapToDto);
        return ResponseEntity.ok(profiles);
    }

    // ADMIN: Voir le detail d'un profil par username
    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserProfileDto> getProfileByUsername(@PathVariable String username) {
        return profileRepository.findByUsername(username)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- KYC : Route de vérification Admin ---
    @PutMapping("/{username}/verify")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserProfileDto> verifyExpertProfile(
            @PathVariable String username,
            @Valid @RequestBody KycVerificationRequest request) {

        UserProfile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Profil non trouve pour " + username));

        profile.setVerificationStatus(request.getStatus());

        UserProfile savedProfile = profileRepository.save(profile);
        securityAuditService.log(
                "USER_PROFILE_VERIFIED_BY_ADMIN",
                savedProfile.getId(),
                SecurityContextHolder.getContext().getAuthentication().getName(),
                "targetUsername=" + username + ", status=" + request.getStatus().name()
        );
        return ResponseEntity.ok(mapToDto(savedProfile));
    }

    // Mapping Entity -> DTO
    private UserProfileDto mapToDto(UserProfile profile) {
        return UserProfileDto.builder()
                .username(profile.getUsername())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .city(profile.getCity())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .preferredLanguage(profile.getPreferredLanguage())
                .bio(profile.getBio())
                .specialties(profile.getSpecialties())
                .professionalTitle(profile.getProfessionalTitle())
                .availability(profile.getAvailability())
                .barreau(profile.getBarreau())
                .numeroToque(profile.getNumeroToque())
                .verificationStatus(
                        profile.getVerificationStatus() != null ? profile.getVerificationStatus().name() : null)
                .companyName(profile.getCompanyName())
                .nif(profile.getNif())
                .employeeCount(profile.getEmployeeCount())
                .subscriptionType(profile.getSubscriptionType())
                .build();
    }
}
