package com.lexdata.auth.controllers;

import com.lexdata.auth.exceptions.RefreshTokenNotFoundException;
import com.lexdata.auth.models.ERole;
import com.lexdata.auth.models.RefreshToken;
import com.lexdata.auth.models.Role;
import com.lexdata.auth.models.User;
import com.lexdata.auth.models.LoginStatus;
import com.lexdata.auth.payload.request.ForgotPasswordRequest;
import com.lexdata.auth.payload.request.LoginRequest;
import com.lexdata.auth.payload.request.ResetPasswordRequest;
import com.lexdata.auth.payload.request.SignupRequest;
import com.lexdata.auth.payload.request.TokenRefreshRequest;
import com.lexdata.auth.payload.response.JwtResponse;
import com.lexdata.auth.payload.response.MessageResponse;
import com.lexdata.auth.payload.response.TokenRefreshResponse;
import com.lexdata.auth.repository.RoleRepository;
import com.lexdata.auth.repository.UserRepository;
import com.lexdata.auth.security.jwt.JwtUtils;
import com.lexdata.auth.security.services.AuditLogService;
import com.lexdata.auth.security.services.EmailVerificationService;
import com.lexdata.auth.security.services.LoginAttemptService;
import com.lexdata.auth.security.services.PasswordResetService;
import com.lexdata.auth.security.services.RefreshTokenService;
import com.lexdata.auth.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;
    private final AuditLogService auditLogService;
    private final com.lexdata.auth.security.services.UserEventPublisher userEventPublisher;

    // ----------------------------------------------------------------
    // Utilitaire : extraire l'IP réelle du client (proxy-aware)
    // ----------------------------------------------------------------
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For peut contenir "ip1, ip2, ip3" – on prend la première
        return xfHeader.split(",")[0].trim();
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String ip = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String username = loginRequest.getUsername();

        if (loginAttemptService.isBlocked(ip, username)) {
            // Audit Log: Blocked IP
            auditLogService.logLoginAttempt(username, null, ip, userAgent, LoginStatus.FAILED_LOCKED);

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse(
                            "Compte temporairement verrouillé suite à de trop nombreuses tentatives (" + ip + "). "
                            + "Veuillez réessayer dans quelques minutes."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // L'authentification a réussi : on réinitialise le cache des tentatives pour
            // cette IP
            loginAttemptService.loginSucceeded(ip, username);

            // Audit Log : Success
            auditLogService.logLoginAttempt(username, userDetails.getId(), ip, userAgent, LoginStatus.SUCCESS);

            String jwt = jwtUtils.generateJwtToken(authentication);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    refreshToken.getToken(),
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles,
                    userDetails.isEmailVerified()));

        } catch (BadCredentialsException ex) {
            // Mot de passe incorrect
            loginAttemptService.loginFailed(ip, username);
            int remaining = loginAttemptService.getRemainingAttempts(ip, username);

            Long userId = userRepository.findByUsername(username).map(User::getId).orElse(null);

            if (remaining <= 0) {
                // Audit Log : FAILED_LOCKED (just triggerd lockout)
                auditLogService.logLoginAttempt(username, userId, ip, userAgent, LoginStatus.FAILED_LOCKED);

                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new MessageResponse(
                                "Compte verrouillé après trop de tentatives. "
                                + "Veuillez réessayer dans 15 minutes."));
            }

            // Audit Log : FAILED_BAD_CREDENTIALS
            auditLogService.logLoginAttempt(username, userId, ip, userAgent, LoginStatus.FAILED_BAD_CREDENTIALS);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse(
                            "Identifiants incorrects. Tentatives restantes avant blocage : " + remaining));
        } catch (DisabledException ex) {
            // Compte présent mais non activé (ex. lien de vérification e-mail non utilisé)
            Long userId = userRepository.findByUsername(username).map(User::getId).orElse(null);
            auditLogService.logLoginAttempt(username, userId, ip, userAgent, LoginStatus.FAILED_DISABLED);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(
                            "Ce compte n'est pas encore activé. Vérifiez votre boîte mail et validez votre adresse "
                                    + "e-mail avant de vous connecter."));
        }
    }

    // --- REFRESH TOKEN ---
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Rotate refresh token on each refresh to reduce replay risk.
                    refreshTokenService.deleteByToken(requestRefreshToken);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
                    String token = jwtUtils.generateTokenFromUser(user);
                    return ResponseEntity.ok(new TokenRefreshResponse(token, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token invalide."));
    }

    // --- LOGOUT (Déconnexion de cet appareil) ---
    @PostMapping("/logout")
    public ResponseEntity<?> logoutDevice(@Valid @RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        refreshTokenService.deleteByToken(refreshToken);
        return ResponseEntity.ok(new MessageResponse("Déconnexion de cet appareil réussie !"));
    }

    // --- LOGOUT ALL (Coupe-circuit / Révocation de toutes les sessions) ---
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllDevices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            refreshTokenService.deleteByUserId(userDetails.getId());
        }
        return ResponseEntity.ok(new MessageResponse("Déconnexion de tous les appareils réussie !"));
    }

    // --- REGISTER ---
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur: Ce nom d'utilisateur est déjà pris !"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Erreur: Cet email est déjà utilisé !"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setTelephone(signUpRequest.getTelephone());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setActive(false); // Inactif jusqu'à vérification email
        user.setEmailVerified(false);

        Set<Role> roles = new HashSet<>();
        // Public signup must always create a standard user account.
        // Elevated roles must be granted via secured admin workflows.
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Erreur: Rôle USER non trouvé."));
        roles.add(userRole);

        user.setRoles(roles);
        userRepository.save(user);

        // Appel asynchrone (Event-Driven) via RabbitMQ
        try {
            com.lexdata.auth.events.UserRegisteredEvent event = com.lexdata.auth.events.UserRegisteredEvent.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .telephone(user.getTelephone())
                    .build();
            userEventPublisher.publishUserRegisteredEvent(event);
        } catch (Exception e) {
            log.error("Erreur de publication de l'event UserRegisteredEvent : {}", e.getMessage());
        }

        // Envoi de l'email de vérification (account activation)
        try {
            emailVerificationService.sendVerificationEmail(user);
        } catch (Exception e) {
            log.error("Erreur d'envoi d'email de vérification pour {} : {}", user.getEmail(), e.getMessage());
        }

        return ResponseEntity.ok(new MessageResponse(
                "Inscription réussie ! Vérifiez votre boîte email pour activer votre compte."));
    }

    // ── FORGOT PASSWORD ─────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Réponse générique indépendamment de l'existence de l'email (anti-enumeration)
        passwordResetService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(new MessageResponse(
                "Si cet email est associé à un compte, vous recevrez un lien de réinitialisation dans quelques minutes."));
    }

    // ── RESET PASSWORD ───────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse(
                "Mot de passe réinitialisé avec succès. Vous pouvez maintenant vous connecter."));
    }

    // ── VERIFY EMAIL ─────────────────────────────────────────────────
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity
                .ok(new MessageResponse("Email vérifié avec succès ! Votre compte est maintenant actif."));
    }

    // ── RESEND VERIFICATION EMAIL ────────────────────────────────────
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam @Email String email) {
        emailVerificationService.resendVerificationEmail(email);
        return ResponseEntity.ok(new MessageResponse("Email de vérification renvoyé avec succès."));
    }
}
