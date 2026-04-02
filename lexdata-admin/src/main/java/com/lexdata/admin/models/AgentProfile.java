package com.lexdata.admin.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_profiles")
public class AgentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    private String fullName;
    private String role; // Ex: JURISTE_SENIOR, MODERATEUR, SUPER_ADMIN
    
    private int activeTasksCount = 0;
    private int completedTasksCount = 0;

    @UpdateTimestamp
    private LocalDateTime lastActivity;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public int getActiveTasksCount() { return activeTasksCount; }
    public void setActiveTasksCount(int activeTasksCount) { this.activeTasksCount = activeTasksCount; }
    public int getCompletedTasksCount() { return completedTasksCount; }
    public void setCompletedTasksCount(int completedTasksCount) { this.completedTasksCount = completedTasksCount; }
    public LocalDateTime getLastActivity() { return lastActivity; }
}
