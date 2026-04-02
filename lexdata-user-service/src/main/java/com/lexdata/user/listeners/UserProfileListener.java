package com.lexdata.user.listeners;

import com.lexdata.user.config.RabbitMQConfig;
import com.lexdata.user.events.UserRegisteredEvent;
import com.lexdata.user.models.UserProfile;
import com.lexdata.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileListener {

    private final UserProfileRepository profileRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_USER_PROFIL_CREATED)
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Événement UserRegisteredEvent reçu pour l'utilisateur '{}' (ID: {})",
                event.getUsername(), event.getId());

        try {
            // Un profil existe peut-être déjà (idempotence)
            UserProfile profile = profileRepository.findByUsername(event.getUsername())
                    .orElse(UserProfile.builder().username(event.getUsername()).build());

            String fullName = event.getFirstName() + " " + event.getLastName();
            profile.setFullName(fullName.trim());
            profile.setPhoneNumber(event.getTelephone());
            // Les autres champs (nif, city...) seront remplis manuellement par
            // l'utilisateur plus tard

            profileRepository.save(profile);
            log.info("✅ Profil créé/mis à jour avec succès pour '{}'", event.getUsername());

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création du profil pour '{}': {}", event.getUsername(), e.getMessage());
            // Une exception ici fera que RabbitMQ remettra le message dans la file (DLQ à
            // configurer par la suite)
            throw e;
        }
    }
}
