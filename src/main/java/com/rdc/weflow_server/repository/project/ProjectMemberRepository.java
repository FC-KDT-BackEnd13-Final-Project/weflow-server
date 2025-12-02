package com.rdc.weflow_server.repository.project;

import com.rdc.weflow_server.entity.project.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // 현재 유저가 속한 모든 프로젝트 조회
    List<ProjectMember> findByUserId(Long userId);

    // 특정 프로젝트 접근 가능 여부 확인
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    // 현재 유저가 속한 모든 프로젝트 조회(삭제된 프로젝트 제외)
    @Query("select pm from ProjectMember pm " +
            "join pm.project p " +
            "where pm.user.id = :userId " +
            "and (p.deletedAt is null or :includeDeleted = true)")
    List<ProjectMember> findByUserIdFiltered(
            @Param("userId") Long userId,
            @Param("includeDeleted") boolean includeDeleted
    );

    // 프로젝트 멤버 조회
    @Query("SELECT pm FROM ProjectMember pm " +
            "JOIN FETCH pm.user u " +
            "JOIN fetch u.company c " +
            "WHERE pm.project.id = :projectId")
    List<ProjectMember> findAllByProjectIdIncludeDeleted(@Param("projectId") Long projectId);


    @Query("SELECT pm FROM ProjectMember pm " +
            "WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    Optional<ProjectMember> findByProjectIdAndUserId(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId
    );
}
