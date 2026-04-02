package com.lexdata.juridique.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LegalAnnotationDto {
    private Long id;
    private Long legalTextId;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
