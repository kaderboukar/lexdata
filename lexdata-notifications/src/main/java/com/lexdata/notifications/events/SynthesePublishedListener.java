package com.lexdata.notifications.events;

import com.lexdata.notifications.config.RabbitConfig;
import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.services.EventDedupService;
import com.lexdata.notifications.services.NotificationService;
import com.lexdata.notifications.services.UserDirectoryService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SynthesePublishedListener {

    private final NotificationService notificationService;
    private final EventDedupService eventDedupService;
    private final UserDirectoryService userDirectoryService;

    @RabbitListener(queues = RabbitConfig.SYNTHESE_PUBLISHED_QUEUE)
    public void handle(SynthesePublishedEvent event) {
        if (event.getId() == null) {
            log.warn("event=synthese_skip reason=null_id");
            return;
        }

        String eventId = event.getEventId() != null && !event.getEventId().isBlank()
                ? event.getEventId()
                : "synthese-" + event.getId();
        if (eventDedupService.isAlreadyProcessed(eventId)) {
            log.debug("event=synthese_duplicate eventId={}", eventId);
            return;
        }

        Set<Long> userIds = new LinkedHashSet<>();
        if (event.getUserIds() != null) {
            userIds.addAll(event.getUserIds());
        }
        if (event.getUsernames() != null) {
            userIds.addAll(userDirectoryService.authUserIdsForUsernames(event.getUsernames()));
        }
        if (userIds.isEmpty()) {
            log.warn("event=synthese_skip reason=no_recipients syntheseId={}", event.getId());
            return;
        }

        String link = event.getLink() != null && !event.getLink().isBlank()
                ? event.getLink()
                : "/synthese/fiches/" + event.getId();
        String titre = event.getTitre() != null ? event.getTitre() : "Nouvelle synthèse disponible";
        String msg = event.getMessage() != null ? event.getMessage() : "Une nouvelle synthèse a été publiée.";

        var names = userDirectoryService.resolveUsernames(userIds);

        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            notificationService.sendNotification(
                    userId,
                    names.get(userId),
                    titre,
                    msg,
                    Notification.NotificationType.SYNTHESE,
                    link);
        }

        eventDedupService.markProcessed(eventId, "SYNTHESIS_PUBLISHED", "lexdata-synthese");
        log.debug("event=synthese_processed eventId={} recipients={}", eventId, userIds.size());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SynthesePublishedEvent implements Serializable {
        private String eventId;
        private Long id;
        private String titre;
        private String message;
        private String link;
        /** Préféré : identifiants auth */
        private List<Long> userIds;
        /** Rétrocompat : noms d'utilisateur */
        private List<String> usernames;
    }
}
