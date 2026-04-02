package com.lexdata.admin.services;

import com.lexdata.admin.models.AdminTask;
import com.lexdata.admin.models.WorkflowHistory;
import com.lexdata.admin.repository.AdminTaskRepository;
import com.lexdata.admin.repository.WorkflowHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminService {

    private final AdminTaskRepository taskRepository;
    private final WorkflowHistoryRepository historyRepository;

    public AdminService(AdminTaskRepository taskRepository, WorkflowHistoryRepository historyRepository) {
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
    }

    public AdminTask createTask(AdminTask task, String creator) {
        task.setCreatedBy(creator);
        task.setStatus(AdminTask.WorkflowStatus.BROUILLON);
        return taskRepository.save(task);
    }

    @Transactional
    public AdminTask updateStatus(Long taskId, AdminTask.WorkflowStatus newStatus, String agent, String comment) {
        return taskRepository.findById(taskId).map(task -> {
            AdminTask.WorkflowStatus oldStatus = task.getStatus();
            task.setStatus(newStatus);
            task.setUpdatedAt(LocalDateTime.now());
            AdminTask updatedTask = taskRepository.save(task);

            WorkflowHistory history = new WorkflowHistory();
            history.setTaskId(taskId);
            history.setFromStatus(oldStatus);
            history.setToStatus(newStatus);
            history.setChangedBy(agent);
            history.setComment(comment);
            historyRepository.save(history);

            return updatedTask;
        }).orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
    }

    public void assignTask(Long taskId, String agentUsername) {
        taskRepository.findById(taskId).ifPresent(task -> {
            task.setAssignedTo(agentUsername);
            taskRepository.save(task);
        });
    }
}
