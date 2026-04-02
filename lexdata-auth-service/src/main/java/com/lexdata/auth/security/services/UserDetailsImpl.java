package com.lexdata.auth.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lexdata.auth.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Optimisation pour les collections (Set)
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Include
    private Long id;

    private String username;

    private String email;

    @JsonIgnore // Sécurité absolue : le mot de passe ne doit pas fuiter
    private String password;

    private boolean active; // Pour gérer le compte activé/désactivé

    /** Aligné sur {@link User#isEmailVerified()} — exposé pour JwtResponse / front. */
    private boolean emailVerified;

    private Collection<? extends GrantedAuthority> authorities;

    // Cette méthode transforme votre Entity "User" en "UserDetails" compréhensible par Spring
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isActive(), // Mappe le champ 'active'
                user.isEmailVerified(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // --- Gestion fine de l'état du compte ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // On pourrait implémenter une expiration ici
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // On pourrait implémenter un verrouillage après 3 échecs
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active; // Si active=false en base, Spring Security bloque le login automatiquement
    }
}