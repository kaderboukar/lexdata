package com.lexdata.synthese.events;

import com.lexdata.synthese.models.FicheSynthetique;
import com.lexdata.synthese.repository.FicheSynthetiqueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JuridiqueEventListener {

    private final FicheSynthetiqueRepository ficheRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "lexdata.queue.texte-modifie")
    @Transactional
    public void handleTexteModifie(TexteModifieEvent event) {
        log.info("🛡️ Bouclier de Cohérence : Détection de modification du texte #{} - {}", event.getId(),
                event.getTitre());

        List<FicheSynthetique> fichesOffençantes = ficheRepository.findByTexteJuridiqueId(event.getId());

        if (fichesOffençantes.isEmpty()) {
            log.info("Aucune fiche synthèse associée au texte #{}. Rien à faire.", event.getId());
            return;
        }

        for (FicheSynthetique fiche : fichesOffençantes) {
            log.warn("⚠️ La fiche #{} ({}) est devenue obsolète suite à la modification du texte juridique.",
                    fiche.getId(), fiche.getTitre());

            fiche.setStatus(FicheSynthetique.SyntheseStatus.ARCHIVED);
            ficheRepository.save(fiche);

            // Notification pour l'Agent Admin
            publishAdminNotification(fiche, event.getModificationSummary());
        }
    }

    private void publishAdminNotification(FicheSynthetique fiche, String modificationSummary) {
        // On publie un message pour le service de notifications
        // On utilise une structure simple que lexdata-notifications saura interpréter
        java.util.Map<String, Object> notification = new java.util.HashMap<>();
        notification.put("type", "ADMIN_ALERT");
        notification.put("message", "La fiche synthèse '" + fiche.getTitre() + "' (ID: " + fiche.getId() + ") " +
                "doit être révisée car le texte juridique source a été modifié : " + modificationSummary);
        notification.put("priority", "HIGH");
        notification.put("targetRole", "ROLE_AGENT_ADMIN");

        rabbitTemplate.convertAndSend("lexdata.exchange.notifications", "notification.admin", notification);
        log.info("Notification d'obsolescence envoyée pour l'Agent Admin (Fiche #{})", fiche.getId());
    }
}
