package com.lexdata.juridique.events;

import com.lexdata.juridique.models.LegalDomain;
import com.lexdata.juridique.models.TypeTexte;
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
    private TypeTexte type;
    private LegalDomain domaine;
    private LocalDate dateSignature;
    private String referenceOfficielle;
}
