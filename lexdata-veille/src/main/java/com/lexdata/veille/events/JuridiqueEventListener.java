package com.lexdata.veille.events;

import com.lexdata.veille.models.AlerteVeille;
import com.lexdata.veille.repository.AlerteVeilleRepository;
import com.lexdata.veille.util.VeilleDomainNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class JuridiqueEventListener {

    private final AlerteVeilleRepository alerteRepository;

    @RabbitListener(queues = "lexdata.queue.texte-publie")
    public void handleTextePublie(TextePublieEvent event) {
        log.info("Réception d'un nouvel événement de publication juridique: {}", event.getTitre());

        if (alerteRepository.existsByTexteJuridiqueIdAndEventTypeAndStatus(
                event.getId(), AlerteVeille.EventType.NEW_TEXT, AlerteVeille.AlertStatus.DRAFT)) {
            log.info("Alerte draft deja existante pour texte {}, ignoree (idempotence).", event.getId());
            return;
        }

        // Création automatique d'un brouillon d'alerte
        AlerteVeille alerte = AlerteVeille.builder()
                .titre("Veille : " + event.getTitre())
                .texteJuridiqueId(event.getId())
                .eventType(AlerteVeille.EventType.NEW_TEXT)
                .texteType(event.getType() == null ? null : event.getType().toUpperCase())
                .datePublicationOfficielle(event.getDateSignature())
                .resumeClarifie("Un nouveau texte juridique a été publié : " + event.getTitre()
                        + ". Veuillez rédiger le résumé clarifié.")
                .status(AlerteVeille.AlertStatus.DRAFT)
                .lienTexte("/api/juridique/textes/" + event.getId())
                .lienSynthese("/api/synthese/fiches/texte/" + event.getId())
                .domainesCibles(canonicalDomainSet(event.getDomaine()))
                .urgence(AlerteVeille.UrgenceLevel.MOYENNE)
                .build();

        alerteRepository.save(alerte);
        log.info("Brouillon d'alerte créé automatiquement pour le texte ID: {}", event.getId());
    }

    @RabbitListener(queues = "lexdata.queue.texte-modifie")
    public void handleTexteModifie(TexteModifieEvent event) {
        log.info("Réception d'un événement de modification juridique: {}", event.getTitre());

        if (alerteRepository.existsByTexteJuridiqueIdAndEventTypeAndStatus(
                event.getId(), AlerteVeille.EventType.UPDATE, AlerteVeille.AlertStatus.DRAFT)) {
            log.info("Alerte UPDATE draft deja existante pour texte {}, ignoree.", event.getId());
            return;
        }

        AlerteVeille alerte = AlerteVeille.builder()
                .titre("Mise a jour : " + event.getTitre())
                .texteJuridiqueId(event.getId())
                .eventType(AlerteVeille.EventType.UPDATE)
                .texteType(event.getType() == null ? null : event.getType().toUpperCase())
                .datePublicationOfficielle(event.getDateSignature())
                .resumeClarifie("Modification importante detectee : " + event.getModificationSummary())
                .status(AlerteVeille.AlertStatus.DRAFT)
                .lienTexte("/api/juridique/textes/" + event.getId())
                .lienSynthese("/api/synthese/fiches/texte/" + event.getId())
                .domainesCibles(canonicalDomainSet(event.getDomaine()))
                .urgence(AlerteVeille.UrgenceLevel.MOYENNE)
                .build();

        alerteRepository.save(alerte);
        log.info("Brouillon d'alerte UPDATE cree pour texte ID: {}", event.getId());
    }

    private static Set<String> canonicalDomainSet(String domaine) {
        String c = VeilleDomainNormalizer.canonical(domaine);
        if (c == null) {
            return new HashSet<>();
        }
        Set<String> s = new HashSet<>();
        s.add(c);
        return s;
    }
}
