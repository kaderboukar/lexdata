package com.lexdata.notifications.services.strategies;

import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.models.NotificationDelivery;
import com.lexdata.notifications.repository.NotificationDeliveryRepository;
import com.lexdata.notifications.repository.NotificationRepository;
import com.lexdata.notifications.services.dispatch.OutboundNotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationStrategy implements NotificationStrategy {

    private static final int MAX_RETRIES = 3;

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void dispatch(OutboundNotificationContext ctx) {
        Notification n = ctx.notification();
        NotificationDelivery d = ctx.delivery();
        d.setLastAttemptAt(java.time.LocalDateTime.now());
        try {
            messagingTemplate.convertAndSendToUser(
                    ctx.username(),
                    "/topic/notifications",
                    new NotificationPayload(
                            n.getTitre(),
                            n.getMessage(),
                            n.getType().name(),
                            n.getLink()));
            n.setStatus(Notification.NotificationStatus.SENT);
            d.setStatus(NotificationDelivery.DeliveryStatus.SENT);
            d.setErrorMessage(null);
            d.setNextRetryAt(null);
            log.debug("event=in_app_sent userId={} notificationId={}", ctx.userId(), n.getId());
        } catch (Exception e) {
            applyFailure(n, d, e);
            log.warn("event=in_app_failed userId={} notificationId={} error={}",
                    ctx.userId(), n.getId(), e.getMessage());
        }
        notificationRepository.save(n);
        deliveryRepository.save(d);
    }

    private void applyFailure(Notification n, NotificationDelivery d, Exception e) {
        int next = d.getRetryCount() + 1;
        d.setRetryCount(next);
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        d.setErrorMessage(msg.length() > 2000 ? msg.substring(0, 2000) : msg);
        n.setStatus(Notification.NotificationStatus.FAILED);
        d.setStatus(NotificationDelivery.DeliveryStatus.FAILED);
        if (next < MAX_RETRIES) {
            long backoffSec = (long) Math.pow(2, next) * 30L;
            d.setNextRetryAt(java.time.LocalDateTime.now().plusSeconds(backoffSec));
        } else {
            d.setNextRetryAt(null);
        }
    }

    @Override
    public Notification.NotificationChannel getChannel() {
        return Notification.NotificationChannel.IN_APP;
    }

    public record NotificationPayload(String title, String message, String type, String link) {
    }
}
