package com.lexdata.admin.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_history")
public class WorkflowHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    @Enumerated(EnumType.STRING)
    private AdminTask.WorkflowStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private AdminTask.WorkflowStatus toStatus;

    private String changedBy;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    private LocalDateTime timestamp;

    // Manual Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public AdminTask.WorkflowStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(AdminTask.WorkflowStatus fromStatus) { this.fromStatus = fromStatus; }
    public AdminTask.WorkflowStatus getToStatus() { return toStatus; }
    public void setToStatus(AdminTask.WorkflowStatus toStatus) { this.toStatus = toStatus; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
