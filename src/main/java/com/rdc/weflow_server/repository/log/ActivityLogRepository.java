package com.rdc.weflow_server.repository.log;

import com.rdc.weflow_server.entity.log.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, ActivityLogRepositoryCustom {
    List<ActivityLog> findTop5ByProjectIdOrderByCreatedAtDesc(Long projectId);
}
