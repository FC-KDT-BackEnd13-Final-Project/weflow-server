package com.rdc.weflow_server.repository.project;

import com.rdc.weflow_server.entity.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectRepositoryCustom {

    @Query("select p from Project p " +
            "left join fetch p.projectMembers pm " +
            "left join fetch pm.user " +
            "where p.id = :projectId")
    Optional<Project> findByIdWithMembers(@Param("projectId") Long projectId);

    // 삭제 조건 포함
    @Query("select p from Project p " +
            "left join fetch p.projectMembers pm " +
            "left join fetch pm.user " +
            "where p.id = :projectId " +
            "and (p.deletedAt is null or :includeDeleted = true )")
    Optional<Project> findByIdWithMembersFiltered(
            @Param("projectId") Long projectId,
            @Param("includeDeleted") boolean includeDeleted);
}
