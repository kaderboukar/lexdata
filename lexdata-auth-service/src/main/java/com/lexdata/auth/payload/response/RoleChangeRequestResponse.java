package com.lexdata.auth.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoleChangeRequestResponse {
    private Long id;
    private Long userId;
    private String username;
    private String requestedRole;
    private String status;
    private String rejectionReason;
    private String processedBy;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
