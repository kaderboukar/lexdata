package com.lexdata.veille.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlerteVeillePublieEvent implements Serializable {
    private Long id;
    private String titre;
    private String message;
    private Set<String> domaines;
    private boolean urgente;
}
