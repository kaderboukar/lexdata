package com.lexdata.admin.controllers;

import com.lexdata.admin.models.AdminTask;
import com.lexdata.admin.models.WorkflowHistory;
import com.lexdata.admin.repository.AdminTaskRepository;
import com.lexdata.admin.repository.AgentProfileRepository;
import com.lexdata.admin.repository.WorkflowHistoryRepository;
import com.lexdata.admin.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminTaskRepository taskRepository;
    private final WorkflowHistoryRepository historyRepository;
    private final AgentProfileRepository profileRepository;

    public AdminController(AdminService adminService, 
                           AdminTaskRepository taskRepository, 
                           WorkflowHistoryRepository historyRepository, 
                           AgentProfileRepository profileRepository) {
        this.adminService = adminService;
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", taskRepository.count());
        stats.put("pendingTasks", taskRepository.findByStatus(AdminTask.WorkflowStatus.EN_RELECTURE).size());
        stats.put("publishedTasks", taskRepository.findByStatus(AdminTask.WorkflowStatus.PUBLIE).size());
        stats.put("totalAgents", profileRepository.count());
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/tasks")
    public ResponseEntity<AdminTask> createTask(@RequestBody AdminTask task) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(adminService.createTask(task, username));
    }

    @GetMapping("/tasks/my")
    public List<AdminTask> getMyTasks() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return taskRepository.findByAssignedTo(username);
    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<AdminTask> updateTaskStatus(@PathVariable Long id, 
                                                    @RequestParam AdminTask.WorkflowStatus status,
                                                    @RequestParam(required = false) String comment) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(adminService.updateStatus(id, status, username, comment));
    }

    @GetMapping("/tasks/{id}/history")
    public List<WorkflowHistory> getTaskHistory(@PathVariable Long id) {
        return historyRepository.findByTaskIdOrderByTimestampDesc(id);
    }
}
