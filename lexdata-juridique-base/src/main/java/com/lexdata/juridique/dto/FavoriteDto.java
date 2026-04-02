package com.lexdata.juridique.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FavoriteDto {
    private Long id;
    private Long legalTextId;
    private String titre;
    private String referenceOfficielle;
    private String domaine;
    private String type;
    private LocalDateTime createdAt;
}
