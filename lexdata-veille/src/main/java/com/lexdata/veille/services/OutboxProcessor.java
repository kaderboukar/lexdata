package com.lexdata.veille.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexdata.veille.config.RabbitConfig;
import com.lexdata.veille.events.AlerteVeillePublieEvent;
import com.lexdata.veille.models.AlerteVeille;
import com.lexdata.veille.models.OutboxEvent;
import com.lexdata.veille.repository.AlerteVeilleRepository;
import com.lexdata.veille.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final AlerteVeilleRepository alerteRepository;
    private final UserAlertService userAlertService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000) // Toutes les 5 secondes
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus(OutboxEvent.OutboxStatus.PENDING);

        if (pendingEvents.isEmpty())
            return;

        log.info("Traitement de {} événements outbox en attente...", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                processEvent(event);
                event.setStatus(OutboxEvent.OutboxStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());
            } catch (Exception e) {
                log.error("Échec du traitement de l'événement outbox {}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxEvent.OutboxStatus.FAILED);
            }
            outboxRepository.save(event);
        }
    }

    private void processEvent(OutboxEvent event) throws Exception {
        Long alerteId = Long.valueOf(event.getAggregateId());

        switch (event.getEventType()) {
            case "ALERTE_PUBLIE_REDIS":
                // Ancien flux outbox : idempotent (unicité user/alerte), collections chargées pour le matching
                AlerteVeille forUserAlerts = alerteRepository.findByIdWithCollections(alerteId)
                        .orElseThrow(() -> new RuntimeException("Alerte non trouvée: " + alerteId));
                userAlertService.createPersonalizedAlerts(forUserAlerts);
                log.info("UserAlert personnalisees generees via Outbox (legacy REDIS) pour alerte {}", alerteId);
                break;

            case "ALERTE_PUBLIE_RABBIT":
                AlerteVeillePublieEvent rabbitEvent = objectMapper.readValue(event.getPayload(),
                        AlerteVeillePublieEvent.class);
                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_ALERTE_PUBLIE,
                        rabbitEvent);
                log.info("Message RabbitMQ envoyé via Outbox pour alerte {}", alerteId);
                break;

            default:
                log.warn("Type d'événement inconnu: {}", event.getEventType());
        }
    }
}
