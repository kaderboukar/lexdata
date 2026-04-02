package com.lexdata.notifications.repository;

import com.lexdata.notifications.models.Notification;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class NotificationSpecifications {

    private NotificationSpecifications() {
    }

    public static Specification<Notification> forActiveUser(Long userId) {
        return (root, q, cb) -> cb.and(
                cb.equal(root.get("userId"), userId),
                cb.isNull(root.get("deletedAt")));
    }

    public static Specification<Notification> unreadOnly(Boolean unreadOnly) {
        return (root, q, cb) -> {
            if (Boolean.TRUE.equals(unreadOnly)) {
                return cb.isFalse(root.get("isRead"));
            }
            return cb.conjunction();
        };
    }

    public static Specification<Notification> typeEquals(Notification.NotificationType type) {
        return (root, q, cb) -> {
            if (type == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("type"), type);
        };
    }

    public static Specification<Notification> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, q, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get("dateCreation"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("dateCreation"), from);
            }
            if (to != null) {
                return cb.lessThanOrEqualTo(root.get("dateCreation"), to);
            }
            return cb.conjunction();
        };
    }
}
