package com.lexdata.notifications.dto;

import com.lexdata.notifications.models.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AdminBroadcastRequest {

    public enum TargetType {
        ALL,
        ROLE,
        USERS
    }

    @NotNull
    private TargetType targetType;

    /** Ex. JURISTE ou ROLE_JURISTE — requis si targetType = ROLE */
    private String role;

    /** Requis si targetType = USERS */
    private List<Long> userIds;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private String link;

    /** Si vide : applique les préférences utilisateur. Sinon force ces canaux. */
    private List<Notification.NotificationChannel> channels;

    private Notification.NotificationType type = Notification.NotificationType.SYSTEME;

    public TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public List<Notification.NotificationChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<Notification.NotificationChannel> channels) {
        this.channels = channels;
    }

    public Notification.NotificationType getType() {
        return type;
    }

    public void setType(Notification.NotificationType type) {
        this.type = type;
    }
}
