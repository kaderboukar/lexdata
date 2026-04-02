package com.lexdata.auth.security.services;

import com.lexdata.auth.models.AuditLog;
import com.lexdata.auth.models.LoginStatus;
import com.lexdata.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service asynchrone chargé d'enregistrer les traces d'audit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Enregistre le résultat d'une tentative de connexion de manière asynchrone
     * (fire-and-forget).
     *
     * @param username  login saisi par l'utilisateur (ex: email)
     * @param userId    ID de l'utilisateur (peut être null si compte non trouvé)
     * @param ipAddress IP extraite via le contrôleur (gère les proxys)
     * @param userAgent Le navigateur ou l'OS du client
     * @param status    Le résultat (Succès ou type d'échec)
     */
    @Async
    public void logLoginAttempt(String username, Long userId, String ipAddress, String userAgent, LoginStatus status) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .userId(userId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .status(status)
                    // createdAt est automatiquement généré par @CreationTimestamp
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("AuditLog sauvegardé : {} depuis {} pour l'utilisateur {}", status, ipAddress, username);
        } catch (Exception e) {
            // L'erreur d'audit ne doit surtout pas bloquer la connexion effective
            log.error("Erreur critique lors de la sauvegarde du log d'audit pour {} : {}", username, e.getMessage());
        }
    }
}
