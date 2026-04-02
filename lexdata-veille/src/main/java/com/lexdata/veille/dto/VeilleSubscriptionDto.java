package com.lexdata.veille.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class VeilleSubscriptionDto {
    private Long id;
    private Set<String> domaines;
    private Set<String> textTypes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
