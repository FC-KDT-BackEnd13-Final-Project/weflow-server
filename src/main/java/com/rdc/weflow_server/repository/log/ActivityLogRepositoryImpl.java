package com.rdc.weflow_server.repository.log;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rdc.weflow_server.dto.log.ActivityLogResponseDto;

import com.rdc.weflow_server.dto.log.ActivityLogStatisticsDto;
import com.rdc.weflow_server.entity.log.ActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.rdc.weflow_server.entity.log.QActivityLog.activityLog;
import static com.rdc.weflow_server.entity.project.QProject.project;
import static com.rdc.weflow_server.entity.user.QUser.user;
import static com.rdc.weflow_server.entity.post.QPost.post;

@RequiredArgsConstructor
public class ActivityLogRepositoryImpl implements ActivityLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ActivityLogResponseDto> searchLogs(
            String actionType,
            String targetTable,
            Long userId,
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 필터
        if (actionType != null) builder.and(activityLog.actionType.stringValue().eq(actionType));
        if (targetTable != null) builder.and(activityLog.targetTable.stringValue().eq(targetTable));
        if (userId != null) builder.and(activityLog.user.id.eq(userId));
        if (projectId != null) builder.and(activityLog.project.id.eq(projectId));

        // 날짜 필터 추가
        if (startDate != null) {
            builder.and(activityLog.createdAt.goe(startDate));
        }

        if (endDate != null) {
            builder.and(activityLog.createdAt.loe(endDate));
        }

        var resultQuery = queryFactory
                .select(
                        Projections.fields(
                                ActivityLogResponseDto.class,
                                activityLog.id.as("logId"),
                                activityLog.actionType.stringValue().as("actionType"),
                                activityLog.targetTable.stringValue().as("targetTable"),
                                activityLog.targetId,
                                activityLog.ipAddress,
                                activityLog.createdAt,
                                user.id.as("userId"),
                                user.name.as("userName"),
                                project.id.as("projectId"),
                                project.name.as("projectName")
                        )
                )
                .from(activityLog)
                .leftJoin(activityLog.user, user)
                .leftJoin(activityLog.project, project)
                .where(builder)
                .orderBy(activityLog.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        var fetch = resultQuery.fetch();

        var countQuery = queryFactory
                .select(activityLog.count())
                .from(activityLog)
                .where(builder);

        return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ActivityLogResponseDto> searchByTarget(String targetTable, Long targetId, Pageable pageable) {
        return searchLogs(null, targetTable, null, null, null, null, pageable);
    }

    @Override
    public Page<ActivityLogResponseDto> searchByUser(Long userId, Pageable pageable) {
        return searchLogs(null, null, userId, null, null, null, pageable);
    }

    @Override
    public Page<ActivityLogResponseDto> searchByProject(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        return searchLogs(null, null, null, projectId, startDate, endDate, pageable);
    }

    @Override // 로그 통계
    public ActivityLogStatisticsDto getStatistics() {
        long total = queryFactory.select(activityLog.count())
                .from(activityLog)
                .fetchOne();

        long create = countByAction(ActionType.CREATE);
        long update = countByAction(ActionType.UPDATE);
        long delete = countByAction(ActionType.DELETE);
        long login = countByAction(ActionType.LOGIN);
        long logout = countByAction(ActionType.LOGOUT);
        long approve = countByAction(ActionType.APPROVE);
        long reject = countByAction(ActionType.REJECT);

        long last7 = queryFactory.select(activityLog.count())
                .from(activityLog)
                .where(activityLog.createdAt.after(LocalDateTime.now().minusDays(7)))
                .fetchOne();

        return ActivityLogStatisticsDto.builder()
                .total(total)
                .createCount(create)
                .updateCount(update)
                .deleteCount(delete)
                .loginCount(login)
                .logoutCount(logout)
                .approveCount(approve)
                .rejectCount(reject)
                .last7Days(last7)
                .build();
    }

    private long countByAction(ActionType action) {
        Long result = queryFactory.select(activityLog.count())
                .from(activityLog)
                .where(activityLog.actionType.eq(action))
                .fetchOne();
        return result != null ? result : 0;
    }

    @Override
    public Page<ActivityLogResponseDto> searchDeletedResources(String targetTable, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(activityLog.targetTable.stringValue().eq(targetTable));

        var result = queryFactory
                .select(
                        Projections.fields(
                                ActivityLogResponseDto.class,
                                activityLog.id.as("logId"),
                                activityLog.actionType.stringValue().as("actionType"),
                                activityLog.targetTable.stringValue().as("targetTable"),
                                activityLog.targetId,
                                activityLog.ipAddress,
                                activityLog.createdAt,
                                user.id.as("userId"),
                                user.name.as("userName"),
                                project.id.as("projectId"),
                                project.name.as("projectName")
                        )
                )
                .from(activityLog)
                .leftJoin(activityLog.user, user)
                .leftJoin(activityLog.project, project)
                .leftJoin(post).on(post.id.eq(activityLog.targetId)) // ← Post 엔티티 조인
                .where(builder.and(post.deletedAt.isNotNull()))
                .orderBy(activityLog.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        var countQuery = queryFactory
                .select(activityLog.count())
                .from(activityLog)
                .leftJoin(post).on(post.id.eq(activityLog.targetId))
                .where(builder.and(post.deletedAt.isNotNull()));

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    // 로그 내보내기
    @Override
    public List<ActivityLogResponseDto> exportSearch(
            String actionType,
            String targetTable,
            Long userId,
            Long projectId,
            String startDate,
            String endDate
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // actionType 필터
        if (actionType != null) {
            builder.and(activityLog.actionType.stringValue().eq(actionType));
        }

        // targetTable 필터
        if (targetTable != null) {
            builder.and(activityLog.targetTable.stringValue().eq(targetTable));
        }

        // userId 필터
        if (userId != null) {
            builder.and(activityLog.user.id.eq(userId));
        }

        // projectId 필터
        if (projectId != null) {
            builder.and(activityLog.project.id.eq(projectId));
        }

        // 날짜 필터
        if (startDate != null) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            builder.and(activityLog.createdAt.goe(start));
        }

        if (endDate != null) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            builder.and(activityLog.createdAt.loe(end));
        }

        return queryFactory
                .select(
                        Projections.fields(
                                ActivityLogResponseDto.class,
                                activityLog.id.as("logId"),
                                activityLog.actionType.stringValue().as("actionType"),
                                activityLog.targetTable.stringValue().as("targetTable"),
                                activityLog.targetId,
                                activityLog.ipAddress,
                                activityLog.createdAt,
                                user.id.as("userId"),
                                user.name.as("userName"),
                                project.id.as("projectId"),
                                project.name.as("projectName")
                        )
                )
                .from(activityLog)
                .leftJoin(activityLog.user, user)
                .leftJoin(activityLog.project, project)
                .where(builder)
                .orderBy(activityLog.createdAt.desc())
                .fetch();
    }
    @Override
    public List<ActivityLogResponseDto> findRecentLogs(int limit) {
        return queryFactory
                .select(
                        Projections.fields(
                                ActivityLogResponseDto.class,
                                activityLog.id.as("logId"),
                                activityLog.actionType.stringValue().as("actionType"),
                                activityLog.targetTable.stringValue().as("targetTable"),
                                activityLog.targetId,
                                activityLog.ipAddress,
                                activityLog.createdAt,
                                user.id.as("userId"),
                                user.name.as("userName"),
                                project.id.as("projectId"),
                                project.name.as("projectName")
                        )
                )
                .from(activityLog)
                .leftJoin(activityLog.user, user)
                .leftJoin(activityLog.project, project)
                .orderBy(activityLog.id.desc())
                .limit(limit)
                .fetch();
    }

}
