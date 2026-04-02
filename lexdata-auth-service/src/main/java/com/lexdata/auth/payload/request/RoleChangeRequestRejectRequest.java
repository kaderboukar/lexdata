package com.lexdata.auth.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleChangeRequestRejectRequest {
    @NotBlank(message = "Le motif du rejet est obligatoire")
    @Size(max = 400, message = "Le motif du rejet est trop long")
    private String reason;
}
