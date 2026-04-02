package com.lexdata.annuaire.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeadRequest {
    @NotBlank
    private String objet;
    @NotBlank
    private String descriptionBesoin;
}
