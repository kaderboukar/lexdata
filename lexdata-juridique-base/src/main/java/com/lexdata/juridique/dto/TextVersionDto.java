package com.lexdata.juridique.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TextVersionDto {
    private Long id;
    private Long legalTextId;
    private String versionLabel;
    private String contenu;
    private LocalDateTime dateVersion;
    private String modificationSummary;
}
