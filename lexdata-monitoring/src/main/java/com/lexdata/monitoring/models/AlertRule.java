package com.lexdata.monitoring.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_rules")
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String metricName; // Ex: http.server.requests.errors
    private String comparisonOperator; // >, <, >=, <=
    private Double threshold;

    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.CRITICAL;

    public enum Severity {
        INFO, WARNING, CRITICAL, FATAL
    }

    @UpdateTimestamp
    private LocalDateTime dateUpdate;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getComparisonOperator() { return comparisonOperator; }
    public void setComparisonOperator(String comparisonOperator) { this.comparisonOperator = comparisonOperator; }
    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public LocalDateTime getDateUpdate() { return dateUpdate; }
}
