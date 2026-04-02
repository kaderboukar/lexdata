package com.lexdata.admin.services;

import com.lexdata.admin.models.AdminTask;
import com.lexdata.admin.models.WorkflowHistory;
import com.lexdata.admin.repository.AdminTaskRepository;
import com.lexdata.admin.repository.WorkflowHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private AdminTaskRepository taskRepository;

    @Mock
    private WorkflowHistoryRepository historyRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void createTask_ShouldSetCreatorAndStatus() {
        // Arrange
        AdminTask task = new AdminTask();
        task.setTitle("Test Task");
        when(taskRepository.save(any(AdminTask.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        AdminTask result = adminService.createTask(task, "creator_user");

        // Assert
        assertEquals("creator_user", result.getCreatedBy());
        assertEquals(AdminTask.WorkflowStatus.BROUILLON, result.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void updateStatus_ShouldUpdateTaskAndCreateHistory() {
        // Arrange
        Long taskId = 1L;
        AdminTask task = new AdminTask();
        task.setId(taskId);
        task.setStatus(AdminTask.WorkflowStatus.BROUILLON);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(AdminTask.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        AdminTask result = adminService.updateStatus(taskId, AdminTask.WorkflowStatus.PUBLIE, "agent_1", "Fait");

        // Assert
        assertEquals(AdminTask.WorkflowStatus.PUBLIE, result.getStatus());
        verify(historyRepository).save(any(WorkflowHistory.class));
        verify(taskRepository).save(task);
    }
}
