package com.rdc.weflow_server.repository.step;

import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StepRequestRepository extends JpaRepository<StepRequest, Long> {

    @EntityGraph(attributePaths = {"step", "requestedBy"})
    List<StepRequest> findByStep_Id(Long stepId);

    @EntityGraph(attributePaths = {"step", "requestedBy"})
    List<StepRequest> findByStep_Project_Id(Long projectId);

    boolean existsByStep_Id(Long stepId);

    List<StepRequest> findByStep_IdOrderByCreatedAtDesc(Long stepId);

    @EntityGraph(attributePaths = {"step", "requestedBy"})
    List<StepRequest> findByStep_Project_IdOrderByCreatedAtDesc(Long projectId);

    boolean existsByStep_IdAndStatus(Long stepId, StepRequestStatus status);

    // 승인 대기 요청 수
    long countByStep_Project_IdInAndStatus(List<Long> projectIds, StepRequestStatus status);

    // 승인 대기 요청 Top 5
    List<StepRequest> findTop5ByStep_Project_IdInAndStatusOrderByCreatedAtDesc(
            List<Long> projectIds,
            StepRequestStatus status
    );
    List<StepRequest> findTop3ByStep_Project_IdOrderByCreatedAtDesc(Long projectId);
    boolean existsByStepIdAndStatus(Long stepId, StepRequestStatus status);
    Optional<StepRequest> findFirstByStep_Project_IdAndStatusOrderByCreatedAtAsc(
            Long projectId,
            StepRequestStatus status
    );

}
