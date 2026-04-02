package com.lexdata.notifications.events;

import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.services.EventDedupService;
import com.lexdata.notifications.services.NotificationService;
import com.lexdata.notifications.services.UserClient;
import com.lexdata.notifications.services.UserDirectoryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class VeilleEventListener {

    private final NotificationService notificationService;
    private final UserClient userClient;
    private final UserDirectoryService userDirectoryService;
    private final EventDedupService eventDedupService;

    @RabbitListener(queues = "lexdata.queue.alerte-publie")
    public void handleAlertePublie(AlerteVeillePublieEvent event) {
        if (event.getId() == null) {
            return;
        }
        String eventId = "veille-alert-" + event.getId();
        if (eventDedupService.isAlreadyProcessed(eventId)) {
            log.debug("event=veille_duplicate eventId={}", eventId);
            return;
        }

        Set<String> domaines = event.getDomaines();
        if (domaines == null || domaines.isEmpty()) {
            return;
        }

        Set<String> allUsernames = domaines.stream()
                .flatMap(domaine -> userClient.getUsernamesByDomain(domaine).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (allUsernames.isEmpty()) {
            return;
        }

        Set<Long> userIds = new LinkedHashSet<>(userDirectoryService.authUserIdsForUsernames(allUsernames));
        var names = userDirectoryService.resolveUsernames(userIds);

        String link = "/veille/alertes/" + event.getId();

        for (Long userId : userIds) {
            notificationService.sendNotification(
                    userId,
                    names.get(userId),
                    event.getTitre(),
                    event.getMessage(),
                    Notification.NotificationType.VEILLE,
                    link);
        }

        eventDedupService.markProcessed(eventId, "ALERTE_VEILLE_PUBLISHED", "lexdata-veille");
        log.debug("event=veille_processed eventId={} recipients={}", eventId, userIds.size());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlerteVeillePublieEvent implements Serializable {
        private Long id;
        private String titre;
        private String message;
        private Set<String> domaines;
        private boolean urgente;
    }
}
