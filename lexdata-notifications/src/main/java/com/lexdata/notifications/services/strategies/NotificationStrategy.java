package com.lexdata.notifications.services.strategies;

import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.services.dispatch.OutboundNotificationContext;

public interface NotificationStrategy {

    void dispatch(OutboundNotificationContext ctx);

    Notification.NotificationChannel getChannel();
}
