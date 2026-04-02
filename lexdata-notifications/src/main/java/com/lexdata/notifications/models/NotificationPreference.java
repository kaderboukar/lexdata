package com.lexdata.notifications.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true, nullable = false)
    private Long userId;

    private boolean emailEnabled = true;
    private boolean smsEnabled = false;
    private boolean pushEnabled = true;
    private boolean inAppEnabled = true;

    @Enumerated(EnumType.STRING)
    private DigestFrequency digestFrequency = DigestFrequency.IMMEDIATE;

    public enum DigestFrequency {
        IMMEDIATE, DAILY
    }

    private boolean veilleAlerts = true;
    private boolean syntheseAlerts = true;
    private boolean calendrierAlerts = true;
    private boolean contratAlerts = true;
    private boolean consultationAlerts = true;
    private boolean paiementAlerts = true;
    private boolean tribuneAlerts = false;

    @UpdateTimestamp
    private LocalDateTime dateUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public boolean isInAppEnabled() {
        return inAppEnabled;
    }

    public void setInAppEnabled(boolean inAppEnabled) {
        this.inAppEnabled = inAppEnabled;
    }

    public boolean isVeilleAlerts() {
        return veilleAlerts;
    }

    public void setVeilleAlerts(boolean veilleAlerts) {
        this.veilleAlerts = veilleAlerts;
    }

    public boolean isSyntheseAlerts() {
        return syntheseAlerts;
    }

    public void setSyntheseAlerts(boolean syntheseAlerts) {
        this.syntheseAlerts = syntheseAlerts;
    }

    public boolean isCalendrierAlerts() {
        return calendrierAlerts;
    }

    public void setCalendrierAlerts(boolean calendrierAlerts) {
        this.calendrierAlerts = calendrierAlerts;
    }

    public boolean isContratAlerts() {
        return contratAlerts;
    }

    public void setContratAlerts(boolean contratAlerts) {
        this.contratAlerts = contratAlerts;
    }

    public boolean isConsultationAlerts() {
        return consultationAlerts;
    }

    public void setConsultationAlerts(boolean consultationAlerts) {
        this.consultationAlerts = consultationAlerts;
    }

    public boolean isPaiementAlerts() {
        return paiementAlerts;
    }

    public void setPaiementAlerts(boolean paiementAlerts) {
        this.paiementAlerts = paiementAlerts;
    }

    public boolean isTribuneAlerts() {
        return tribuneAlerts;
    }

    public void setTribuneAlerts(boolean tribuneAlerts) {
        this.tribuneAlerts = tribuneAlerts;
    }

    public DigestFrequency getDigestFrequency() {
        return digestFrequency;
    }

    public void setDigestFrequency(DigestFrequency digestFrequency) {
        this.digestFrequency = digestFrequency;
    }

    public LocalDateTime getDateUpdate() {
        return dateUpdate;
    }
}
