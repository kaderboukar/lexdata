package com.lexdata.veille.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextePublieEvent implements Serializable {
    private Long id;
    private String titre;
    private String type; // Reçu comme String depuis le JSON (Enum.name())
    private String domaine; // Reçu comme String depuis le JSON (Enum.name())
    private LocalDate dateSignature;
    private String referenceOfficielle;
}
