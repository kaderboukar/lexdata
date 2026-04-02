package com.lexdata.admin.repository;

import com.lexdata.admin.models.WorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, Long> {
    List<WorkflowHistory> findByTaskIdOrderByTimestampDesc(Long taskId);
}
