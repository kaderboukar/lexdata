package com.lexdata.auth.controllers;

import com.lexdata.auth.dto.UserDto; // Assurez-vous que ce package correspond au vôtre
import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.Role;
import com.lexdata.auth.models.User;
import com.lexdata.auth.repository.RoleRepository;
import com.lexdata.auth.repository.UserRepository;
import com.lexdata.auth.security.services.SecurityAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/users")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecurityAuditService securityAuditService;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, SecurityAuditService securityAuditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.securityAuditService = securityAuditService;
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // =========================================================================
    // 1. LISTER ET RECHERCHER LES UTILISATEURS
    // =========================================================================
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(required = false, defaultValue = "") String search,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // Utilisation de la méthode de recherche personnalisée du UserRepository
        Page<User> users = userRepository.searchUsers(search, pageable);

        // Conversion de l'entité User vers UserDto pour masquer le mot de passe
        Page<UserDto> userDtos = users.map(this::mapToDto);

        return ResponseEntity.ok(userDtos);
    }

    // =========================================================================
    // 2. CHANGER LE RÔLE D'UN UTILISATEUR
    // =========================================================================
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newRoleStr = body.get("role"); // Exemple reçu de React : "AVOCAT" ou "JURISTE"

        if (newRoleStr == null || newRoleStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le rôle ne peut pas être vide."));
        }

        // Ajout sécurisé du préfixe "ROLE_" si le Frontend l'a oublié
        String enumRoleName = newRoleStr.startsWith("ROLE_") ? newRoleStr.toUpperCase()
                : "ROLE_" + newRoleStr.toUpperCase();

        try {
            // Tentative de conversion de la chaîne en valeur de notre enum ERole
            ERole eRole = ERole.valueOf(enumRoleName);

            return userRepository.findById(id).map(user -> {
                // Recherche du rôle en base de données
                Role role = roleRepository.findByName(eRole)
                        .orElseThrow(() -> new RuntimeException(
                                "Erreur: Le rôle " + eRole + " n'existe pas en base de données."));

                // Remplacement des anciens rôles par le nouveau
                Set<Role> roles = new HashSet<>();
                roles.add(role);
                user.setRoles(roles);

                // Sauvegarde de l'utilisateur mis à jour
                userRepository.save(user);
                securityAuditService.log(
                        "USER_ROLE_UPDATED_DIRECT",
                        user.getId(),
                        SecurityContextHolder.getContext().getAuthentication().getName(),
                        "newRole=" + eRole.name());

                return ResponseEntity.ok(Map.of("message", "Le rôle de l'utilisateur a été mis à jour avec succès."));

            }).orElse(ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {
            // Si valueOf() échoue (ex: on lui passe "ROLE_MAGICIEN")
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Erreur: Le rôle '" + newRoleStr + "' n'est pas un rôle valide du système."));
        } catch (RuntimeException e) {
            // Si le rôle existe dans l'Enum mais pas dans la table PostgreSQL/MySQL
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    // =========================================================================
    // 3. SUPPRIMER UN UTILISATEUR
    // =========================================================================
    @Transactional
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    // SOFT DELETE : On marque comme supprimé, on ne touche pas à la DB
                    // Cela préserve l'audit trail (indispensable pour une plateforme juridique)
                    // La restriction @SQLRestriction("is_deleted = false") masquera
                    // automatiquement cet utilisateur dans toutes les prochaines requêtes
                    user.setDeleted(true);
                    user.setActive(false);
                    userRepository.save(user);
                    securityAuditService.log(
                            "USER_DELETED_SOFT",
                            user.getId(),
                            SecurityContextHolder.getContext().getAuthentication().getName(),
                            "softDelete=true");
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================================
    // UTILITAIRES : MAPPER (Entity -> DTO)
    // =========================================================================
    private UserDto mapToDto(User user) {
        // Récupération de la liste des rôles sous forme de Strings (ex: ["ROLE_USER"])
        List<String> rolesNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        // Assurez-vous que votre UserDto a bien un constructeur Builder ou adaptez
        // cette partie
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .telephone(user.getTelephone())
                .roles(rolesNames)
                // .createdAt(user.getCreatedAt()) // Décommentez si vous gérez les dates de
                // création
                .build();
    }
}
