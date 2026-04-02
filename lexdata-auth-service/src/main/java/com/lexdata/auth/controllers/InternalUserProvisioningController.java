package com.lexdata.auth.controllers;

import com.lexdata.auth.dto.internal.InternalProvisionUserRequest;
import com.lexdata.auth.dto.internal.InternalProvisionUserResponse;
import com.lexdata.auth.events.UserRegisteredEvent;
import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.Role;
import com.lexdata.auth.models.User;
import com.lexdata.auth.payload.response.MessageResponse;
import com.lexdata.auth.repository.RoleRepository;
import com.lexdata.auth.repository.UserRepository;
import com.lexdata.auth.security.services.EmailVerificationService;
import com.lexdata.auth.security.services.UserEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provisioning utilisateur (rôle + email vérifié) réservé aux appels internes munis de
 * {@code X-Lexdata-Internal-Key}.
 */
@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalUserProvisioningController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/users/provision")
    public ResponseEntity<?> provisionUser(@Valid @RequestBody InternalProvisionUserRequest body) {
        if (userRepository.existsByUsername(body.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur: Ce nom d'utilisateur est déjà pris !"));
        }
        if (userRepository.existsByEmail(body.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur: Cet email est déjà utilisé !"));
        }

        ERole requestedRole = resolveRole(body.getRole());

        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new IllegalArgumentException("Rôle introuvable en base: " + requestedRole));

        boolean verified = body.isEmailVerified();
        User user = new User();
        user.setUsername(body.getUsername());
        user.setEmail(body.getEmail());
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        user.setTelephone(body.getTelephone());
        user.setFirstName(body.getFirstName());
        user.setLastName(body.getLastName());
        user.setEmailVerified(verified);
        user.setActive(verified);
        user.setPendingValidation(false);
        user.setRoles(new HashSet<>(Set.of(role)));

        userRepository.save(user);

        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .telephone(user.getTelephone())
                    .build();
            userEventPublisher.publishUserRegisteredEvent(event);
        } catch (Exception e) {
            log.warn("Publication UserRegisteredEvent ignorée après provision {} : {}", user.getUsername(),
                    e.getMessage());
        }

        if (!verified) {
            try {
                emailVerificationService.sendVerificationEmail(user);
            } catch (Exception e) {
                log.error("Erreur d'envoi d'email de vérification après provision pour {} : {}", user.getEmail(),
                        e.getMessage());
            }
        }

        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new InternalProvisionUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleNames,
                user.isEmailVerified(),
                user.isActive()));
    }

    private static ERole resolveRole(String roleInput) {
        if (roleInput == null || roleInput.isBlank()) {
            return ERole.ROLE_USER;
        }
        String r = roleInput.trim().toUpperCase();
        if (!r.startsWith("ROLE_")) {
            r = "ROLE_" + r;
        }
        try {
            return ERole.valueOf(r);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle inconnu: " + roleInput);
        }
    }
}
