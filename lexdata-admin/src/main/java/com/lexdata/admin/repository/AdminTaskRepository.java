package com.lexdata.admin.repository;

import com.lexdata.admin.models.AdminTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminTaskRepository extends JpaRepository<AdminTask, Long> {
    List<AdminTask> findByAssignedTo(String username);
    List<AdminTask> findByStatus(AdminTask.WorkflowStatus status);
}
