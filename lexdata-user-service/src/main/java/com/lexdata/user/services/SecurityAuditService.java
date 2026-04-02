package com.lexdata.user.services;

import com.lexdata.user.models.SecurityAuditEvent;
import com.lexdata.user.repository.SecurityAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityAuditService {

    private final SecurityAuditEventRepository securityAuditEventRepository;

    @Async
    public void log(String action, Long userId, String performedBy, String details) {
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .action(action)
                .userId(userId)
                .performedBy(performedBy == null ? "system" : performedBy)
                .details(details)
                .build();
        securityAuditEventRepository.save(event);
    }
}
