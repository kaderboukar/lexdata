package com.lexdata.juridique.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnnotationRequest {
    @NotBlank(message = "Le contenu de l'annotation est obligatoire")
    @Size(max = 2000, message = "L'annotation ne doit pas depasser 2000 caracteres")
    private String note;
}
