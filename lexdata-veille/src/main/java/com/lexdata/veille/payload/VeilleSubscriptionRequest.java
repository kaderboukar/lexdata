package com.lexdata.veille.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class VeilleSubscriptionRequest {
    @NotEmpty(message = "Au moins un domaine est requis")
    private Set<String> domaines;

    @NotEmpty(message = "Au moins un type de texte est requis")
    private Set<String> textTypes;

    @NotNull(message = "Le statut active/inactive est requis")
    private Boolean active;
}
