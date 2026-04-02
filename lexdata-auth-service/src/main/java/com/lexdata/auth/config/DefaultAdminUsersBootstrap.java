package com.lexdata.auth.config;

import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.Role;
import com.lexdata.auth.models.User;
import com.lexdata.auth.repository.RoleRepository;
import com.lexdata.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Crée au premier démarrage deux comptes techniques : SUPER_ADMIN et AGENT_ADMIN.
 * Idempotent : ne recrée pas si le {@code username} existe déjà.
 */
@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DefaultAdminUsersBootstrap {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(20)
    CommandLineRunner seedDefaultAdminUsers(
            @Value("${lexdata.bootstrap.super-admin-password}") String superAdminPassword,
            @Value("${lexdata.bootstrap.agent-admin-password}") String agentAdminPassword) {
        return args -> {
            ensureAdminUser("superadmin", "superadmin@lexdata.local", ERole.ROLE_SUPER_ADMIN,
                    superAdminPassword, "Super", "Administrateur");
            ensureAdminUser("agentadmin", "agentadmin@lexdata.local", ERole.ROLE_AGENT_ADMIN,
                    agentAdminPassword, "Agent", "Administrateur");
        };
    }

    private void ensureAdminUser(String username, String email, ERole roleEnum, String rawPassword,
            String firstName, String lastName) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Bootstrap admin: l'email {} est déjà utilisé, compte {} ignoré.", email, username);
            return;
        }
        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new IllegalStateException(
                        "Rôle introuvable: " + roleEnum + " (RoleInitializer doit s'exécuter avant)."));

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setTelephone("0000000000");
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setActive(true);
        u.setEmailVerified(true);
        u.setPendingValidation(false);
        u.setRoles(Set.of(role));
        userRepository.save(u);
        log.info("Compte système créé: {} — rôle {} (changez le mot de passe en production).", username,
                roleEnum.name());
    }
}
