package com.lexdata.synthese.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SyntheseVersionDto {
    private Long id;
    private Long ficheId;
    private String contenuJson;
    private String agentEmail;
    private String commentaireVersion;
    private Long texteJuridiqueVersionId;
    private LocalDateTime dateVersion;
}
