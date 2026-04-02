package com.lexdata.notifications.services;

import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.models.NotificationDelivery;
import com.lexdata.notifications.repository.NotificationDeliveryRepository;
import com.lexdata.notifications.repository.NotificationRepository;
import com.lexdata.notifications.services.dispatch.OutboundNotificationContext;
import com.lexdata.notifications.services.strategies.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeliveryRetryService {

    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationRepository notificationRepository;
    private final Map<Notification.NotificationChannel, NotificationStrategy> strategyMap;
    private final UserDirectoryService userDirectoryService;

    @Value("${lexdata.notifications.retry.max-attempts:3}")
    private int maxAttempts;

    public DeliveryRetryService(
            NotificationDeliveryRepository deliveryRepository,
            NotificationRepository notificationRepository,
            List<NotificationStrategy> strategies,
            UserDirectoryService userDirectoryService) {
        this.deliveryRepository = deliveryRepository;
        this.notificationRepository = notificationRepository;
        this.strategyMap = strategies.stream().collect(Collectors.toMap(NotificationStrategy::getChannel, s -> s));
        this.userDirectoryService = userDirectoryService;
    }

    @Scheduled(fixedDelayString = "${lexdata.notifications.retry.poll-ms:60000}")
    @Transactional
    public void retryFailedDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationDelivery> batch = deliveryRepository.findDueForRetry(
                NotificationDelivery.DeliveryStatus.FAILED,
                maxAttempts,
                now,
                PageRequest.of(0, 50));

        if (batch.isEmpty()) {
            return;
        }

        log.debug("event=delivery_retry_batch size={}", batch.size());

        for (NotificationDelivery delivery : batch) {
            Notification notification = notificationRepository.findById(delivery.getNotificationId()).orElse(null);
            if (notification == null) {
                log.warn("event=delivery_retry_skip reason=missing_notification deliveryId={}", delivery.getId());
                continue;
            }
            NotificationStrategy strategy = strategyMap.get(delivery.getChannel());
            if (strategy == null) {
                continue;
            }
            String username = userDirectoryService.resolveUsername(delivery.getUserId(), null);
            strategy.dispatch(new OutboundNotificationContext(delivery.getUserId(), username, notification, delivery));
        }
    }
}
