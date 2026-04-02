package com.lexdata.notifications.events;

import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAlertListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @RabbitListener(queues = "lexdata.queue.admin-alerts")
    public void handleAdminAlert(Map<String, Object> message) {
        String alertText = message.get("message") != null ? message.get("message").toString() : "";
        log.debug("event=admin_alert_ws messageLen={}", alertText.length());

        messagingTemplate.convertAndSend("/topic/admin/alerts", message);

        Long userId = extractUserId(message.get("userId"));
        if (userId == null) {
            log.debug("event=admin_alert_skip_notification reason=no_userId");
            return;
        }

        Object linkObj = message.get("link");
        String link = linkObj != null ? linkObj.toString() : null;
        notificationService.sendNotification(
                userId,
                null,
                "Alerte Système administrative",
                alertText,
                Notification.NotificationType.SYSTEME,
                link);
    }

    private static Long extractUserId(Object raw) {
        if (raw instanceof Number n) {
            return n.longValue();
        }
        if (raw instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
