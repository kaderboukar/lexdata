package com.lexdata.notifications.services.strategies;

import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.models.NotificationDelivery;
import com.lexdata.notifications.repository.NotificationDeliveryRepository;
import com.lexdata.notifications.repository.NotificationRepository;
import com.lexdata.notifications.services.dispatch.OutboundNotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationStrategy implements NotificationStrategy {

    private static final int MAX_RETRIES = 3;

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;

    @Override
    public void dispatch(OutboundNotificationContext ctx) {
        Notification n = ctx.notification();
        NotificationDelivery d = ctx.delivery();
        d.setLastAttemptAt(LocalDateTime.now());
        try {
            log.debug("event=push_simulated userId={} notificationId={}", ctx.userId(), n.getId());
            n.setStatus(Notification.NotificationStatus.SENT);
            d.setStatus(NotificationDelivery.DeliveryStatus.SENT);
            d.setErrorMessage(null);
            d.setNextRetryAt(null);
        } catch (Exception e) {
            applyFailure(n, d, e);
            log.warn("event=push_failed userId={} notificationId={} error={}",
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
            d.setNextRetryAt(LocalDateTime.now().plusSeconds(backoffSec));
        } else {
            d.setNextRetryAt(null);
        }
    }

    @Override
    public Notification.NotificationChannel getChannel() {
        return Notification.NotificationChannel.PUSH;
    }
}
