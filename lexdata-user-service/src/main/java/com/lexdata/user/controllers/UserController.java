package com.lexdata.user.controllers;

import com.lexdata.user.dto.UserAggregateDto;
import com.lexdata.user.dto.UserPreferenceDto;
import com.lexdata.user.dto.UserProfileDto;
import com.lexdata.user.dto.UserUpdateRequest;
import com.lexdata.user.models.UserPreference;
import com.lexdata.user.models.UserProfile;
import com.lexdata.user.models.VerificationStatus;
import com.lexdata.user.repository.UserPreferenceRepository;
import com.lexdata.user.repository.UserProfileRepository;
import com.lexdata.user.services.UserDeletionPublisher;
import com.lexdata.user.services.SecurityAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserProfileRepository profileRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final UserDeletionPublisher userDeletionPublisher;
    private final SecurityAuditService securityAuditService;

    // --- 1. GET /me : Récupérer profil + préférences ---
    @GetMapping("/me")
    public ResponseEntity<UserAggregateDto> getMyAggregateProfile() {
        return ResponseEntity.ok(buildAggregateProfile(getCurrentUsername()));
    }

    // --- 2. PUT /me : Mettre à jour profil + préférences ---
    @PutMapping("/me")
    public ResponseEntity<UserAggregateDto> updateMyAggregateProfile(@Valid @RequestBody UserUpdateRequest request) {
        String username = getCurrentUsername();

        // Profil
        UserProfile profile = profileRepository.findByUsername(username)
                .orElse(UserProfile.builder().username(username).build());

        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setCity(request.getCity());
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profile.setPreferredLanguage(request.getPreferredLanguage());
        profile.setBio(request.getBio());
        profile.setSpecialties(request.getSpecialties());
        profile.setProfessionalTitle(request.getProfessionalTitle());
        profile.setAvailability(request.getAvailability());
        profile.setCompanyName(request.getCompanyName());
        profile.setNif(request.getNif());
        profile.setEmployeeCount(request.getEmployeeCount());

        // --- LOGIQUE KYC ---
        // Si les champs d'expertise (barreau, numeroToque) sont modifiés, on repasse en
        // PENDING
        boolean kycChanged = false;

        if (request.getBarreau() != null && !request.getBarreau().equals(profile.getBarreau())) {
            profile.setBarreau(request.getBarreau());
            kycChanged = true;
        }

        if (request.getNumeroToque() != null && !request.getNumeroToque().equals(profile.getNumeroToque())) {
            profile.setNumeroToque(request.getNumeroToque());
            kycChanged = true;
        }

        if (kycChanged) {
            profile.setVerificationStatus(VerificationStatus.PENDING);
            log.info("L'utilisateur {} a modifié ses données KYC. Statut repassé à PENDING.", username);
        }

        UserProfile savedProfile = profileRepository.save(profile);

        // Préférences
        UserPreference preference = preferenceRepository.findByUserProfile(profile)
                .orElse(UserPreference.builder().userProfile(savedProfile).build());

        preference.setFollowedTopics(request.getFollowedTopics());
        preference.setAlertKeywords(request.getAlertKeywords());
        preference.setEmailEnabled(request.isEmailEnabled());
        preference.setPushEnabled(request.isPushEnabled());
        preference.setSmsEnabled(request.isSmsEnabled());
        if (request.getTimezone() != null) {
            preference.setTimezone(request.getTimezone());
        }

        preferenceRepository.save(preference);

        log.info("Profil & Préférences mis à jour via PUT /me pour {}", username);
        securityAuditService.log("USER_PROFILE_UPDATED", savedProfile.getId(), username, "self-service update");
        return ResponseEntity.ok(buildAggregateProfile(username));
    }

    // --- 3. GET /me/export : Export JSON RGPD ---
    @GetMapping("/me/export")
    public ResponseEntity<UserAggregateDto> exportMyData() {
        UserAggregateDto aggregate = buildAggregateProfile(getCurrentUsername());

        HttpHeaders headers = new HttpHeaders();
        // Content-Disposition oblige le navigateur à télécharger le fichier
        headers.setContentDispositionFormData("attachment", "rgpd_export.json");

        log.info("Export RGPD généré pour {}", getCurrentUsername());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(aggregate);
    }

    // --- 4. DELETE /me : Droit à l'oubli (Anonymisation) ---
    @DeleteMapping("/me")
    public ResponseEntity<?> anonymizeMyProfile() {
        String username = getCurrentUsername();

        UserProfile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Profil non trouve"));

        // Anonymisation des données personnelles
        profile.setFullName("Utilisateur Supprimé");
        profile.setPhoneNumber(null);
        profile.setCity(null);
        profile.setProfilePictureUrl(null);
        profile.setBio(null);
        profile.setCompanyName(null);
        profile.setNif(null);
        // ... (Conserve specialties ou metadata pertinentes pour les stats si besoin)

        profileRepository.save(profile);

        // Réinitialisation des préférences (désactiver les envois)
        preferenceRepository.findByUserProfile(profile).ifPresent(p -> {
            p.setEmailEnabled(false);
            p.setPushEnabled(false);
            p.setSmsEnabled(false);
            p.setAlertKeywords(null);
            p.setFollowedTopics(null);
            preferenceRepository.save(p);
        });

        log.info("Profil anonymisé (RGPD) pour {}", username);
        securityAuditService.log("USER_PROFILE_ANONYMIZED", profile.getId(), username, "self-service delete/anonymize");

        // Publier l'Event RabbitMQ
        try {
            userDeletionPublisher.publishUserDeletedEvent(username);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'event UserDeletedEvent pour {} : {}", username, e.getMessage());
        }

        return ResponseEntity.ok().body("Votre profil a été anonymisé avec succès (Droit à l'oubli).");
    }

    // --- Utilitaires internes ---

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private UserAggregateDto buildAggregateProfile(String username) {
        UserProfile profile = profileRepository.findByUsername(username)
                .orElse(UserProfile.builder().username(username).build());

        UserProfileDto profileDto = mapProfileToDto(profile);

        // Si le profil existe, on cherche ses préférences, sinon on renvoie un objet
        // vide
        UserPreference preference = null;
        if (profile.getId() != null) {
            preference = preferenceRepository.findByUserProfile(profile).orElse(null);
        }

        return UserAggregateDto.builder()
                .profile(profileDto)
                .preference(mapPreferenceToDto(preference))
                .build();
    }

    private UserPreferenceDto mapPreferenceToDto(UserPreference preference) {
        if (preference == null) {
            return null;
        }
        return UserPreferenceDto.builder()
                .followedTopics(preference.getFollowedTopics())
                .alertKeywords(preference.getAlertKeywords())
                .emailEnabled(preference.isEmailEnabled())
                .pushEnabled(preference.isPushEnabled())
                .smsEnabled(preference.isSmsEnabled())
                .timezone(preference.getTimezone())
                .build();
    }

    private UserProfileDto mapProfileToDto(UserProfile profile) {
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
