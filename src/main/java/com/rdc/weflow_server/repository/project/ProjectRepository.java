package com.rdc.weflow_server.repository.project;

import com.rdc.weflow_server.entity.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, ProjectRepositoryCustom {

    // 상세 조회 (멤버 포함)
    @Query("select p from Project p " +
            "left join fetch p.projectMembers pm " +
            "left join fetch pm.user " +
            "where p.id = :projectId")
    Optional<Project> findByIdWithMembers(@Param("projectId") Long projectId);

    // 상세 조회 (삭제 포함 옵션)
    @Query("select p from Project p " +
            "left join fetch p.projectMembers pm " +
            "left join fetch pm.user " +
            "where p.id = :projectId " +
            "and (p.deletedAt is null or :includeDeleted = true )")
    Optional<Project> findByIdWithMembersFiltered(
            @Param("projectId") Long projectId,
            @Param("includeDeleted") boolean includeDeleted);

    // 1) 모든 프로젝트 조회 (관리자 / 개발사용)
    @Query("select p from Project p where p.deletedAt is null order by p.createdAt desc")
    List<Project> findAllActiveProjects();

    // 2) 고객사: 본인 프로젝트만 조회
    @Query("select pm.project from ProjectMember pm " +
            "where pm.user.id = :userId and pm.deletedAt is null and pm.project.deletedAt is null " +
            "order by pm.project.createdAt desc")
    List<Project> findActiveProjectsByUser(@Param("userId") Long userId);

    @Query("select p from Project p " +
            "join p.projectMembers pm " +
            "where pm.user.id = :userId " +
            "and pm.deletedAt is null and p.deletedAt is null " +
            "order by p.createdAt desc")
    List<Project> findActiveProjectsByUserOrderByCreatedDesc(@Param("userId") Long userId);

    // 삭제되지 않은 프로젝트 수
    @Query("SELECT COUNT(p) FROM Project p WHERE p.deletedAt IS NULL")
    long countActiveProjects();
}
