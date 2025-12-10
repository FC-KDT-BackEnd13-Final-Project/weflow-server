package com.rdc.weflow_server.repository.project;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rdc.weflow_server.entity.project.Project;
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
    public List<Project> searchAdminProjects(ProjectStatus status, Long companyId, String keyword, int page, int size) {
        QProject project = QProject.project;

        return query
                .selectFrom(project)
                .where(
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
    public long countAdminProjects(ProjectStatus status, Long companyId, String keyword) {
        QProject project = QProject.project;

        return Optional.ofNullable(
                query.select(project.count())
                        .from(project)
                        .where(
                                eqStatus(status),
                                eqCompany(companyId),
                                containsName(keyword)
                        )
                        .fetchOne()
        ).orElse(0L);
    }

    /** MY PROJECTS */
    @Override
    public List<Project> searchMyProjects(Long userId, UserRole role, String keyword, int page, int size) {
        QProject project = QProject.project;
        QProjectMember member = QProjectMember.projectMember;

        return query
                .selectFrom(project)
                .leftJoin(project.projectMembers, member).on(member.user.id.eq(userId))
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
            case SYSTEM_ADMIN, AGENCY -> project.id.isNotNull(); // 전체 접근
            case CLIENT -> member.user.id.eq(userId);            // 본인 프로젝트만
            default -> null;
        };
    }
}
