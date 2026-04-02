package com.lexdata.veille.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UserAlertDto {
    private Long id;
    private Long alertId;
    private String userId;
    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    private String title;
    private String summary;
    private String eventType;
    private String status;
    private Long legalTextId;
    private String textType;
    private Set<String> domaines;
    private String legalTextUrl;
    private String syntheseUrl;
    private LocalDateTime alertDate;
}
