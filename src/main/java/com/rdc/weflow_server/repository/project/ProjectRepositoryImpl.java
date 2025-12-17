package com.rdc.weflow_server.repository.project;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rdc.weflow_server.dto.project.response.ProjectSummaryResponse;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.project.QProject;
import com.rdc.weflow_server.entity.project.QProjectMember;
import com.rdc.weflow_server.entity.user.UserRole;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<Project> searchAdminProjects(ProjectPhase phase, ProjectStatus status, Long companyId, String keyword, int page, int size) {
        QProject project = QProject.project;

        return query
                .selectFrom(project)
                .where(
                        eqPhase(phase),
                        eqStatus(status),
                        eqCompany(companyId),
                        containsName(keyword)
                )
                .orderBy(project.createdAt.desc())
                .offset(page * size)
                .limit(size)
                .fetch();
    }

    @Override
    public long countAdminProjects(ProjectPhase phase, ProjectStatus status, Long companyId, String keyword) {
        QProject project = QProject.project;

        return Optional.ofNullable(
                query.select(project.count())
                        .from(project)
                        .where(
                                eqPhase(phase),
                                eqStatus(status),
                                eqCompany(companyId),
                                containsName(keyword)
                        )
                        .fetchOne()
        ).orElse(0L);
    }

    /** MY PROJECTS */
    @Override
    public List<ProjectSummaryResponse> searchMyProjects(Long userId, UserRole role, String keyword, int page, int size) {
        QProject project = QProject.project;
        QProjectMember member = QProjectMember.projectMember;

        return query
                .select(
                        Projections.constructor(
                                ProjectSummaryResponse.class,
                                project.id,
                                project.name,
                                project.phase,
                                project.status,
                                project.company.name,
                                project.expectedEndDate,

                                // ⭐ isMember 계산
                                JPAExpressions
                                        .selectOne()
                                        .from(member)
                                        .where(
                                                member.project.id.eq(project.id),
                                                member.user.id.eq(userId),
                                                member.deletedAt.isNull()
                                        )
                                        .exists()
                        )
                )
                .from(project)
                .where(
                        roleFilter(role, userId),
                        containsName(keyword),
                        project.deletedAt.isNull()
                )
                .orderBy(project.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    @Override
    public long countMyProjects(Long userId, UserRole role, String keyword) {
        QProject project = QProject.project;
        QProjectMember member = QProjectMember.projectMember;

        return Optional.ofNullable(
                query.select(project.count())
                        .from(project)
                        .leftJoin(project.projectMembers, member).on(member.user.id.eq(userId))
                        .where(
                                roleFilter(role, userId),
                                containsName(keyword),
                                project.deletedAt.isNull()
                        )
                        .fetchOne()
        ).orElse(0L);
    }

    // ========================
    // Helper Expressions
    // ========================

    private BooleanExpression eqPhase(ProjectPhase phase) {
        return phase != null ? QProject.project.phase.eq(phase) : null;
    }

    private BooleanExpression eqStatus(ProjectStatus status) {
        return status != null ? QProject.project.status.eq(status) : null;
    }

    private BooleanExpression eqCompany(Long companyId) {
        return companyId != null ? QProject.project.company.id.eq(companyId) : null;
    }

    private BooleanExpression containsName(String keyword) {
        return (keyword != null && !keyword.isBlank())
                ? QProject.project.name.containsIgnoreCase(keyword)
                : null;
    }

    private BooleanExpression roleFilter(UserRole role, Long userId) {
        QProject project = QProject.project;
        QProjectMember member = QProjectMember.projectMember;

        return switch (role) {
            case SYSTEM_ADMIN, AGENCY -> project.id.isNotNull();

            case CLIENT ->
                    JPAExpressions
                            .selectOne()
                            .from(member)
                            .where(
                                    member.project.id.eq(project.id),
                                    member.user.id.eq(userId),
                                    member.deletedAt.isNull()
                            )
                            .exists();

            default -> null;
        };
    }
}
