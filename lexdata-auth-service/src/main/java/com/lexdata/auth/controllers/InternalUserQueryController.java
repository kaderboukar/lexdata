package com.lexdata.auth.controllers;

import com.lexdata.auth.dto.internal.IdUsernameDto;
import com.lexdata.auth.dto.internal.UserIdPageResponse;
import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.User;
import com.lexdata.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * API interne (clé X-Lexdata-Internal-Key) pour résolution d'utilisateurs (notifications, etc.).
 */
@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
public class InternalUserQueryController {

    private final UserRepository userRepository;

    @PostMapping("/user-ids/by-usernames")
    public List<Long> findUserIdsByUsernames(@RequestBody List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String u : usernames) {
            if (u != null && !u.isBlank()) {
                unique.add(u.trim());
            }
        }
        if (unique.isEmpty()) {
            return List.of();
        }
        return userRepository.findIdsByUsernamesIn(unique);
    }

    @PostMapping("/users/resolve")
    public List<IdUsernameDto> resolveIdsToUsernames(@RequestBody List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<User> users = userRepository.findAllById(new LinkedHashSet<>(userIds));
        return users.stream().map(u -> new IdUsernameDto(u.getId(), u.getUsername())).toList();
    }

    @GetMapping("/user-ids")
    public UserIdPageResponse listUserIds(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 1000));
        Page<Long> result;
        if (role == null || role.isBlank()) {
            result = userRepository.findAllUserIds(pageable);
        } else {
            ERole erole = parseRole(role);
            result = userRepository.findUserIdsByRole(erole, pageable);
        }
        return new UserIdPageResponse(
                result.getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber());
    }

    private static ERole parseRole(String role) {
        String r = role.trim().toUpperCase();
        if (!r.startsWith("ROLE_")) {
            r = "ROLE_" + r;
        }
        try {
            return ERole.valueOf(r);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle inconnu: " + role);
        }
    }
}
