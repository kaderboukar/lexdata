package com.lexdata.contrats.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "contracts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String ownerUsername;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private ContractModel model;

    @NotBlank
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String generatedContent;

    @ElementCollection
    @CollectionTable(name = "contract_values", joinColumns = @JoinColumn(name = "contract_id"))
    @MapKeyColumn(name = "variable_name")
    @Column(name = "variable_value")
    @Builder.Default
    private Map<String, String> fieldValues = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ContractStatus status = ContractStatus.BROUILLON;

    private LocalDateTime dateEcheance;
    private boolean rappelEnvoye = false;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContractSignature> signatures = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public ContractModel getModel() { return model; }
    public void setModel(ContractModel model) { this.model = model; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getGeneratedContent() { return generatedContent; }
    public void setGeneratedContent(String generatedContent) { this.generatedContent = generatedContent; }
    public Map<String, String> getFieldValues() { return fieldValues; }
    public void setFieldValues(Map<String, String> fieldValues) { this.fieldValues = fieldValues; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public LocalDateTime getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDateTime dateEcheance) { this.dateEcheance = dateEcheance; }
    public boolean isRappelEnvoye() { return rappelEnvoye; }
    public void setRappelEnvoye(boolean rappelEnvoye) { this.rappelEnvoye = rappelEnvoye; }
    public List<ContractSignature> getSignatures() { return signatures; }
    public void setSignatures(List<ContractSignature> signatures) { this.signatures = signatures; }

    public enum ContractStatus {
        BROUILLON, EN_NEGOCIATION, EN_ATTENTE_SIGNATURE, SIGNE, EN_COURS, TERMINE, RESILIE
    }
}
