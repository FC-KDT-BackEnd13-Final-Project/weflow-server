package com.rdc.weflow_server.repository.step;

import com.rdc.weflow_server.entity.step.Step;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StepRepository extends JpaRepository<Step, Long> {

    // 프로젝트별 단계 목록 (순서 기준)
    @EntityGraph(attributePaths = {"project", "createdBy"})
    List<Step> findByProject_IdAndDeletedAtIsNullOrderByOrderIndexAsc(Long projectId);

    @Query("select max(s.orderIndex) from Step s where s.project.id = :projectId and s.deletedAt is null")
    Integer findMaxOrderIndexByProjectId(Long projectId);

    List<Step> findByProject_IdAndIdInAndDeletedAtIsNull(Long projectId, Collection<Long> stepIds);

    boolean existsByProject_IdAndTitleIgnoreCaseAndDeletedAtIsNull(Long projectId, String title);

    boolean existsByProject_IdAndTitleIgnoreCaseAndIdNotAndDeletedAtIsNull(Long projectId, String title, Long id);

    boolean existsByProject_IdAndOrderIndexAndDeletedAtIsNull(Long projectId, Integer orderIndex);

    Optional<Step> findByIdAndDeletedAtIsNull(Long id);
}
