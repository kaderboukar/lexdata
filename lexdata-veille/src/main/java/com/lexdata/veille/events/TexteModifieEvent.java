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
public class TexteModifieEvent implements Serializable {
    private Long id;
    private String titre;
    private String type;
    private String domaine;
    private LocalDate dateSignature;
    private String modificationSummary;
}
