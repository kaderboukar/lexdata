package com.lexdata.veille.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexdata.veille.events.AlerteVeillePublieEvent;
import com.lexdata.veille.models.AlerteVeille;
import com.lexdata.veille.models.OutboxEvent;
import com.lexdata.veille.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestre les effets de bord lors de la première publication d'une alerte :
 * notification (logs / hooks), création immédiate des {@link com.lexdata.veille.models.UserAlert}
 * pour les abonnés actifs, puis enregistrement outbox pour diffusion RabbitMQ.
 */
@Service
@RequiredArgsConstructor
public class AlertePublicationService {

    private final NotificationService notificationService;
    private final UserAlertService userAlertService;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void onFirstPublication(AlerteVeille alerteAvecCollections) {
        notificationService.dispatchAlerte(alerteAvecCollections);
        userAlertService.createPersonalizedAlerts(alerteAvecCollections);

        try {
            AlerteVeillePublieEvent rabbitPayload = AlerteVeillePublieEvent.builder()
                    .id(alerteAvecCollections.getId())
                    .titre(alerteAvecCollections.getTitre())
                    .message(alerteAvecCollections.getResumeClarifie())
                    .domaines(alerteAvecCollections.getDomainesCibles())
                    .urgente(alerteAvecCollections.getUrgence() == AlerteVeille.UrgenceLevel.ELEVEE)
                    .build();

            outboxRepository.save(OutboxEvent.builder()
                    .aggregateId(String.valueOf(alerteAvecCollections.getId()))
                    .eventType("ALERTE_PUBLIE_RABBIT")
                    .payload(objectMapper.writeValueAsString(rabbitPayload))
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Erreur de sérialisation outbox Rabbit", e);
        }
    }
}
