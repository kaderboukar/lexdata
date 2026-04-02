package com.lexdata.contrats.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractInstanceRequest {
    private Long modelId;
    @NotBlank
    private String titre;
    private Map<String, String> fieldValues;
    private LocalDateTime dateEcheance;

    public Long getModelId() { return modelId; }
    public String getTitre() { return titre; }
    public Map<String, String> getFieldValues() { return fieldValues; }
    public LocalDateTime getDateEcheance() { return dateEcheance; }
}
