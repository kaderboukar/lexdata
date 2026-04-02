package com.lexdata.synthese.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TexteModifieEvent implements Serializable {
    private Long id;
    private String titre;
    private String modificationSummary;
}
