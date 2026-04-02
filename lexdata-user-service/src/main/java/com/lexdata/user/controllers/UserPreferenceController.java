package com.lexdata.user.controllers;

import com.lexdata.user.dto.UserPreferenceDto;
import com.lexdata.user.models.LegalDomain;
import com.lexdata.user.models.UserPreference;
import com.lexdata.user.models.UserProfile;
import com.lexdata.user.repository.UserPreferenceRepository;
import com.lexdata.user.repository.UserProfileRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceRepository preferenceRepository;
    private final UserProfileRepository profileRepository;

    @Data
    static class PreferenceRequest {
        private Set<LegalDomain> followedTopics;
        @Size(max = 50, message = "Trop de mots-cles")
        private Set<String> alertKeywords;
        private boolean emailEnabled;
        private boolean pushEnabled;
        private boolean smsEnabled;
        @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+$", message = "Timezone invalide")
        private String timezone;
    }

    @GetMapping
    public ResponseEntity<UserPreferenceDto> getMyPreferences() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return preferenceRepository.findByUserProfileUsername(username)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserPreferenceDto> updatePreferences(@Valid @RequestBody PreferenceRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        UserProfile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Profil non trouve"));

        UserPreference preference = preferenceRepository.findByUserProfile(profile)
                .orElse(UserPreference.builder().userProfile(profile).build());

        preference.setFollowedTopics(request.getFollowedTopics());
        preference.setAlertKeywords(request.getAlertKeywords());
        preference.setEmailEnabled(request.isEmailEnabled());
        preference.setPushEnabled(request.isPushEnabled());
        preference.setSmsEnabled(request.isSmsEnabled());
        preference.setTimezone(request.getTimezone());

        return ResponseEntity.ok(toDto(preferenceRepository.save(preference)));
    }

    @GetMapping("/by-domain/{domain}")
    public java.util.List<String> getUsernamesByDomain(@PathVariable LegalDomain domain) {
        return preferenceRepository.findUsernamesByFollowedTopic(domain);
    }

    private UserPreferenceDto toDto(UserPreference preference) {
        return UserPreferenceDto.builder()
                .followedTopics(preference.getFollowedTopics())
                .alertKeywords(preference.getAlertKeywords())
                .emailEnabled(preference.isEmailEnabled())
                .pushEnabled(preference.isPushEnabled())
                .smsEnabled(preference.isSmsEnabled())
                .timezone(preference.getTimezone())
                .build();
    }
}
