package com.rdc.weflow_server.repository.step;

import com.rdc.weflow_server.entity.step.StepRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepRequestRepository extends JpaRepository<StepRequest, Long> {

    @EntityGraph(attributePaths = {"step", "requestedBy"})
    List<StepRequest> findByStep_Id(Long stepId);

    @EntityGraph(attributePaths = {"step", "requestedBy"})
    List<StepRequest> findByStep_Project_Id(Long projectId);
}
