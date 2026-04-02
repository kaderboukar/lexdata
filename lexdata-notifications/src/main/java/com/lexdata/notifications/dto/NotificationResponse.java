package com.lexdata.notifications.dto;

import com.lexdata.notifications.models.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String titre,
        String message,
        String link,
        Notification.NotificationType type,
        boolean read,
        LocalDateTime dateCreation,
        LocalDateTime dateLecture) {

    public static NotificationResponse fromEntity(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitre(),
                n.getMessage(),
                n.getLink(),
                n.getType(),
                n.isRead(),
                n.getDateCreation(),
                n.getDateLecture());
    }
}
