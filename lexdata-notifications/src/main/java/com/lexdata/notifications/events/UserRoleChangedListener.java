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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRoleChangedListener {

    private final NotificationService notificationService;
    private final EventDedupService eventDedupService;
    private final UserDirectoryService userDirectoryService;

    @RabbitListener(queues = RabbitConfig.USER_ROLE_CHANGED_QUEUE)
    public void handle(UserRoleChangedEvent event) {
        Long userId = event.getUserId();
        if (userId == null && event.getUsername() != null && !event.getUsername().isBlank()) {
            List<Long> ids = userDirectoryService.authUserIdsForUsernames(List.of(event.getUsername().trim()));
            userId = ids.isEmpty() ? null : ids.get(0);
        }
        if (userId == null) {
            log.warn("event=user_role_skip reason=no_user_id");
            return;
        }

        String eventId = event.getEventId() != null && !event.getEventId().isBlank()
                ? event.getEventId()
                : "user-role-" + userId + "-" + (event.getNewRole() != null ? event.getNewRole() : "");
        if (eventDedupService.isAlreadyProcessed(eventId)) {
            log.debug("event=user_role_duplicate eventId={}", eventId);
            return;
        }

        String titre = event.getTitre() != null ? event.getTitre() : "Mise à jour de votre compte";
        String msg = event.getMessage() != null ? event.getMessage()
                : (event.getNewRole() != null
                        ? "Votre rôle a été mis à jour : " + event.getNewRole()
                        : "Votre profil ou vos droits ont été modifiés.");

        String usernameHint = event.getUsername();
        notificationService.sendNotification(
                userId,
                usernameHint,
                titre,
                msg,
                Notification.NotificationType.SYSTEME,
                event.getLink());

        eventDedupService.markProcessed(eventId, "USER_ROLE_CHANGED", "lexdata-auth");
        log.debug("event=user_role_processed eventId={} userId={}", eventId, userId);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRoleChangedEvent implements Serializable {
        private String eventId;
        private Long userId;
        private String username;
        private String newRole;
        private String titre;
        private String message;
        private String link;
    }
}
