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
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "contract_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String templateContent; // Contenu avec variables type {{nom_partie}}

    private String categorie; // Travail, Bail, Vente, etc.

    @ElementCollection
    @CollectionTable(name = "contract_model_variables", joinColumns = @JoinColumn(name = "model_id"))
    @MapKeyColumn(name = "variable_name")
    @Column(name = "variable_description")
    @Builder.Default
    private Map<String, String> variablesRequired = new HashMap<>();

    @Builder.Default
    private boolean actif = true;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTemplateContent() { return templateContent; }
    public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public Map<String, String> getVariablesRequired() { return variablesRequired; }
    public void setVariablesRequired(Map<String, String> variablesRequired) { this.variablesRequired = variablesRequired; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
