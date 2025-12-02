package com.rdc.weflow_server.repository.step;

import com.rdc.weflow_server.entity.step.StepRequestHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StepRequestHistoryRepository extends JpaRepository<StepRequestHistory, Long> {

    @EntityGraph(attributePaths = {"request", "updatedBy"})
    @Query("select h from StepRequestHistory h where h.request.id = :requestId order by h.createdAt asc")
    List<StepRequestHistory> findAscByRequestId(@Param("requestId") Long requestId);

    @EntityGraph(attributePaths = {"request", "updatedBy"})
    @Query("select h from StepRequestHistory h where h.request.id = :requestId order by h.createdAt desc")
    List<StepRequestHistory> findDescByRequestId(@Param("requestId") Long requestId);
}
