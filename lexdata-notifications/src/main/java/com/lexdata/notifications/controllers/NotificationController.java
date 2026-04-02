package com.lexdata.notifications.controllers;

import com.lexdata.notifications.dto.NotificationResponse;
import com.lexdata.notifications.models.Notification;
import com.lexdata.notifications.models.NotificationPreference;
import com.lexdata.notifications.repository.NotificationPreferenceRepository;
import com.lexdata.notifications.repository.NotificationRepository;
import com.lexdata.notifications.repository.NotificationSpecifications;
import com.lexdata.notifications.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) Notification.NotificationType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Specification<Notification> spec = Specification
                .where(NotificationSpecifications.forActiveUser(userId))
                .and(NotificationSpecifications.unreadOnly(unreadOnly))
                .and(NotificationSpecifications.typeEquals(type))
                .and(NotificationSpecifications.createdBetween(from, to));

        Page<Notification> result = notificationRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateCreation")));

        return ResponseEntity.ok(result.map(NotificationResponse::fromEntity));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return patchRead(id, true);
    }

    @PatchMapping("/{id}/unread")
    public ResponseEntity<NotificationResponse> markAsUnread(@PathVariable Long id) {
        return patchRead(id, false);
    }

    private ResponseEntity<NotificationResponse> patchRead(Long id, boolean read) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return notificationRepository.findById(id)
                .filter(n -> userId.equals(n.getUserId()) && n.getDeletedAt() == null)
                .map(n -> {
                    n.setRead(read);
                    n.setDateLecture(read ? LocalDateTime.now() : null);
                    return ResponseEntity.ok(NotificationResponse.fromEntity(notificationRepository.save(n)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return notificationRepository.findById(id)
                .filter(n -> userId.equals(n.getUserId()) && n.getDeletedAt() == null)
                .<ResponseEntity<Void>>map(n -> {
                    n.setDeletedAt(LocalDateTime.now());
                    notificationRepository.save(n);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreference> getPreferences() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return preferenceRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreference> updatePreferences(
            @Valid @RequestBody NotificationPreference incoming) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        NotificationPreference prefs = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference p = new NotificationPreference();
                    p.setUserId(userId);
                    return p;
                });

        prefs.setUserId(userId);
        prefs.setEmailEnabled(incoming.isEmailEnabled());
        prefs.setSmsEnabled(incoming.isSmsEnabled());
        prefs.setPushEnabled(incoming.isPushEnabled());
        prefs.setInAppEnabled(incoming.isInAppEnabled());
        prefs.setDigestFrequency(incoming.getDigestFrequency());
        prefs.setVeilleAlerts(incoming.isVeilleAlerts());
        prefs.setSyntheseAlerts(incoming.isSyntheseAlerts());
        prefs.setCalendrierAlerts(incoming.isCalendrierAlerts());
        prefs.setContratAlerts(incoming.isContratAlerts());
        prefs.setConsultationAlerts(incoming.isConsultationAlerts());
        prefs.setPaiementAlerts(incoming.isPaiementAlerts());
        prefs.setTribuneAlerts(incoming.isTribuneAlerts());

        return ResponseEntity.ok(preferenceRepository.save(prefs));
    }
}
