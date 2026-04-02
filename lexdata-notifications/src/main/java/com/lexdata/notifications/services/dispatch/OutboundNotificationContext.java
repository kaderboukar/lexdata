package com.lexdata.notifications.services.dispatch;

import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.models.NotificationDelivery;

public record OutboundNotificationContext(
        Long userId,
        String username,
        Notification notification,
        NotificationDelivery delivery) {
}
