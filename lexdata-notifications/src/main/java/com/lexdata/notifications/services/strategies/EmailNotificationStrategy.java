package com.lexdata.notifications.services.strategies;

import com.lexdata.notifications.config.RabbitConfig;
import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.models.NotificationDelivery;
import com.lexdata.notifications.repository.NotificationDeliveryRepository;
import com.lexdata.notifications.repository.NotificationRepository;
import com.lexdata.notifications.services.dispatch.OutboundNotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationStrategy implements NotificationStrategy {

    private static final int MAX_RETRIES = 3;

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void dispatch(OutboundNotificationContext ctx) {
        Notification n = ctx.notification();
        NotificationDelivery d = ctx.delivery();
        d.setLastAttemptAt(LocalDateTime.now());
        try {
            String body = n.getLink() != null && !n.getLink().isBlank()
                    ? n.getMessage() + "\n\nLien : " + n.getLink()
                    : n.getMessage();

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(ctx.username() + "@lexdata.com");
            mail.setSubject("[LEXDATA] " + n.getTitre());
            mail.setText(body);
            mail.setFrom("no-reply@lexdata.com");

            mailSender.send(mail);

            n.setStatus(Notification.NotificationStatus.SENT);
            d.setStatus(NotificationDelivery.DeliveryStatus.SENT);
            d.setErrorMessage(null);
            d.setNextRetryAt(null);
            log.debug("event=email_sent userId={} notificationId={}", ctx.userId(), n.getId());
        } catch (Exception e) {
            applyFailure(ctx, n, d, e);
            log.warn("event=email_failed userId={} notificationId={} error={}",
                    ctx.userId(), n.getId(), e.getMessage());
        }
        notificationRepository.save(n);
        deliveryRepository.save(d);
    }

    private void applyFailure(OutboundNotificationContext ctx, Notification n, NotificationDelivery d, Exception e) {
        int next = d.getRetryCount() + 1;
        d.setRetryCount(next);
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        d.setErrorMessage(msg.length() > 2000 ? msg.substring(0, 2000) : msg);
        n.setStatus(Notification.NotificationStatus.FAILED);
        d.setStatus(NotificationDelivery.DeliveryStatus.FAILED);

        if (next >= MAX_RETRIES) {
            d.setNextRetryAt(null);
            publishDlq(ctx, n, d, e);
            log.warn("event=email_delivery_exhausted userId={} notificationId={}", ctx.userId(), n.getId());
        } else {
            long backoffSec = (long) Math.pow(2, next) * 30L;
            d.setNextRetryAt(LocalDateTime.now().plusSeconds(backoffSec));
            log.debug("event=email_retry_scheduled userId={} attempt={} nextRetryAt={}",
                    ctx.userId(), next, d.getNextRetryAt());
        }
    }

    private void publishDlq(OutboundNotificationContext ctx, Notification n, NotificationDelivery d, Exception e) {
        Map<String, Object> deadMessage = new HashMap<>();
        deadMessage.put("userId", ctx.userId());
        deadMessage.put("username", ctx.username());
        deadMessage.put("notificationId", n.getId());
        deadMessage.put("deliveryId", d.getId());
        deadMessage.put("titre", n.getTitre());
        deadMessage.put("message", n.getMessage());
        deadMessage.put("type", n.getType().name());
        deadMessage.put("link", n.getLink());
        deadMessage.put("error", e.getMessage());
        deadMessage.put("timestamp", LocalDateTime.now().toString());

        rabbitTemplate.convertAndSend(RabbitConfig.DLX_NAME, RabbitConfig.DLQ_ROUTING_KEY, deadMessage);
    }

    @Override
    public Notification.NotificationChannel getChannel() {
        return Notification.NotificationChannel.EMAIL;
    }

    /**
     * Résumé quotidien : envoi SMTP sans création de {@link NotificationDelivery} (évite double comptage).
     */
    public void sendDigestSummary(Long userId, String username, String titre, String plainBody) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(username + "@lexdata.com");
            mail.setSubject("[LEXDATA] " + titre);
            mail.setText(plainBody);
            mail.setFrom("no-reply@lexdata.com");
            mailSender.send(mail);
            log.debug("event=digest_email_sent userId={}", userId);
        } catch (Exception e) {
            log.warn("event=digest_email_failed userId={} error={}", userId, e.getMessage());
        }
    }
}
