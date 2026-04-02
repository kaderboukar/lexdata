package com.lexdata.auth.config;

import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.Role;
import com.lexdata.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @Bean
    public CommandLineRunner initRoles() {
        return args -> {
            createRoleIfNotFound(ERole.ROLE_USER);
            createRoleIfNotFound(ERole.ROLE_JURISTE);
            createRoleIfNotFound(ERole.ROLE_AVOCAT);
            createRoleIfNotFound(ERole.ROLE_AGENT_ADMIN);
            createRoleIfNotFound(ERole.ROLE_SUPER_ADMIN);
        };
    }

    private void createRoleIfNotFound(ERole roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }
}
