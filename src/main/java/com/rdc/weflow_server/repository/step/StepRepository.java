package com.rdc.weflow_server.repository.step;

import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {

    // 프로젝트별 단계 목록 (순서 기준)
    @Query("""
            select s from Step s
            where s.project.id = :projectId
              and s.deletedAt is null
            order by
              case s.phase
                when com.rdc.weflow_server.entity.project.ProjectPhase.CONTRACT then 1
                when com.rdc.weflow_server.entity.project.ProjectPhase.IN_PROGRESS then 2
                when com.rdc.weflow_server.entity.project.ProjectPhase.DELIVERY then 3
                when com.rdc.weflow_server.entity.project.ProjectPhase.MAINTENANCE then 4
                else 999
              end,
              s.orderIndex asc,
              s.id asc
            """)
    @EntityGraph(attributePaths = {"project", "createdBy"})
    List<Step> findByProject_IdAndDeletedAtIsNullOrderByOrderIndexAsc(Long projectId);

    @Query("""
            select s from Step s
            where s.project.id = :projectId
              and s.phase = :phase
              and s.deletedAt is null
            order by s.orderIndex asc, s.id asc
            """)
    @EntityGraph(attributePaths = {"project", "createdBy"})
    List<Step> findByProject_IdAndPhaseAndDeletedAtIsNullOrderByOrderIndexAsc(Long projectId, ProjectPhase phase);
    @Query("""
            select s from Step s
            where s.project.id = :projectId
              and s.deletedAt is null
              and s.id in :stepIds
            order by
              case s.phase
                when com.rdc.weflow_server.entity.project.ProjectPhase.CONTRACT then 1
                when com.rdc.weflow_server.entity.project.ProjectPhase.IN_PROGRESS then 2
                when com.rdc.weflow_server.entity.project.ProjectPhase.DELIVERY then 3
                when com.rdc.weflow_server.entity.project.ProjectPhase.MAINTENANCE then 4
                else 999
              end,
              s.orderIndex asc,
              s.id asc
            """)
    List<Step> findByProject_IdAndIdInAndDeletedAtIsNull(Long projectId, Collection<Long> stepIds);

    boolean existsByProject_IdAndTitleIgnoreCaseAndDeletedAtIsNull(Long projectId, String title);

    boolean existsByProject_IdAndTitleIgnoreCaseAndIdNotAndDeletedAtIsNull(Long projectId, String title, Long id);

    @Query("select max(s.orderIndex) from Step s where s.project.id = :projectId and s.phase = :phase and s.deletedAt is null")
    Integer findMaxOrderIndexByProjectIdAndPhase(Long projectId, ProjectPhase phase);

    boolean existsByProject_IdAndPhaseAndOrderIndexAndDeletedAtIsNull(Long projectId, ProjectPhase phase, Integer orderIndex);

    Optional<Step> findByIdAndDeletedAtIsNull(Long id);
    @Query("""
            select s from Step s
            where s.project.id = :projectId
            order by
              case s.phase
                when com.rdc.weflow_server.entity.project.ProjectPhase.CONTRACT then 1
                when com.rdc.weflow_server.entity.project.ProjectPhase.IN_PROGRESS then 2
                when com.rdc.weflow_server.entity.project.ProjectPhase.DELIVERY then 3
                when com.rdc.weflow_server.entity.project.ProjectPhase.MAINTENANCE then 4
                else 999
              end,
              s.orderIndex asc,
              s.id asc
            """)
    List<Step> findByProjectIdOrderByOrderIndexAsc(Long projectId);
    Optional<Step> findTopByProject_IdAndOrderIndexAndDeletedAtIsNullOrderByIdAsc(Long projectId, Integer orderIndex);
}
