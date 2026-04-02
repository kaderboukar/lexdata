package com.lexdata.user.dto;

import com.lexdata.user.models.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KycVerificationRequest {
    @NotNull(message = "Le statut de vérification est obligatoire")
    private VerificationStatus status;
    private String comment;
}
