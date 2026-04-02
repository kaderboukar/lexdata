package com.lexdata.notifications.controllers;

import com.lexdata.notifications.dto.AdminBroadcastRequest;
import com.lexdata.notifications.services.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','AGENT_ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> send(@Valid @RequestBody AdminBroadcastRequest request) {
        notificationService.broadcast(request);
        return ResponseEntity.accepted().build();
    }
}
