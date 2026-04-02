package com.lexdata.auth.security.services;

import com.lexdata.auth.config.RabbitMQConfig;
import com.lexdata.auth.events.UserDeletedEvent;
import com.lexdata.auth.models.User;
import com.lexdata.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionListener {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_USER_DELETED)
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Événement UserDeletedEvent reçu pour l'utilisateur '{}'", event.getUsername());

        try {
            Optional<User> userOpt = userRepository.findByUsername(event.getUsername());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // On coche isDeleted pour conserver les logs d'audit à l'avenir si besoin
                user.setDeleted(true);
                // On peut aussi le désactiver pour bloquer de futures tentatives de connexion
                user.setActive(false);
                userRepository.save(user);

                // On révoque toutes les sessions en cours
                refreshTokenService.deleteByUserId(user.getId());

                log.info("✅ Compte utilisateur '{}' désactivé et sessions révoquées avec succès (Droit à l'oubli).",
                        event.getUsername());
            } else {
                log.warn("L'utilisateur '{}' n'a pas été trouvé dans auth-service pour la suppression.",
                        event.getUsername());
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression/désactivation de '{}': {}", event.getUsername(), e.getMessage());
            throw e; // Laisse RabbitMQ remettre en file
        }
    }
}
