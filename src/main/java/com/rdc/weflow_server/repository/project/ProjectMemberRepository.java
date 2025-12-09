package com.rdc.weflow_server.repository.project;

import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * 일반 유저가 속한 프로젝트 목록
     * - 삭제된 멤버 제외 (pm.deletedAt IS NULL)
     * - 삭제된 프로젝트 제외 (p.deletedAt IS NULL)
     * - 프로젝트 생성일 DESC 정렬
     */
    @Query("select pm from ProjectMember pm " +
            "where pm.project.id = :projectId " +
            "and pm.deletedAt is null " +
            "order by pm.createdAt desc")
    List<ProjectMember> findActiveByProjectId(Long projectId);

    // 프로젝트 접근 확인용
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    // 알림 발송용: 삭제되지 않은 멤버 목록
    List<ProjectMember> findByProjectIdAndDeletedAtIsNull(Long projectId);

    /**
     * 해당 프로젝트의 active(삭제되지 않은) 멤버인지 검증
     */
    @Query("select pm from ProjectMember pm " +
            "where pm.project.id = :projectId " +
            "and pm.user.id = :userId " +
            "and pm.deletedAt is null")
    Optional<ProjectMember> findActiveByProjectIdAndUserId(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId
    );

    /** 카운트용(삭제멤버 제외) */
    @Query("select count(pm) from ProjectMember pm " +
            "where pm.user.id = :userId " +
            "and pm.deletedAt is null " +
            "and pm.project.status = :status " +
            "and pm.project.deletedAt is null")
    long countActiveMembershipByUserIdAndProjectStatus(
            @Param("userId") Long userId,
            @Param("status") ProjectStatus status
    );

    /** 관리자용 조회(삭제된 멤버 모두 포함) */
    @Query("SELECT pm FROM ProjectMember pm " +
            "JOIN FETCH pm.user u " +
            "LEFT JOIN FETCH u.company c " +
            "WHERE pm.project.id = :projectId")
    List<ProjectMember> findAllByProjectIdIncludeDeleted(@Param("projectId") Long projectId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
