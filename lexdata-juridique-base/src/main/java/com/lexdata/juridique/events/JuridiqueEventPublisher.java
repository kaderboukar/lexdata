package com.lexdata.juridique.events;

import com.lexdata.juridique.config.RabbitConfig;
import com.lexdata.juridique.models.TexteJuridique;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JuridiqueEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishTextePublie(TexteJuridique texte) {
        TextePublieEvent event = TextePublieEvent.builder()
                .id(texte.getId())
                .titre(texte.getTitre())
                .type(texte.getType())
                .domaine(texte.getDomaine())
                .dateSignature(texte.getDateSignature())
                .referenceOfficielle(texte.getReferenceOfficielle())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitConfig.JURIDIQUE_EXCHANGE,
                RabbitConfig.TEXTE_PUBLIE_ROUTING_KEY,
                event);
        log.info("Événement de publication envoyé pour le texte: {}", texte.getId());
    }

    public void publishTexteModifie(TexteJuridique texte, String summary) {
        TexteModifieEvent event = TexteModifieEvent.builder()
                .id(texte.getId())
                .titre(texte.getTitre())
                .modificationSummary(summary)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitConfig.JURIDIQUE_EXCHANGE,
                RabbitConfig.TEXTE_MODIFIE_ROUTING_KEY,
                event);
        log.info("Événement de modification envoyé pour le texte: {}", texte.getId());
    }
}
