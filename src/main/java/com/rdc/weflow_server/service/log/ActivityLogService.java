package com.rdc.weflow_server.service.log;

import com.rdc.weflow_server.dto.log.ActivityLogListResponseDto;
import com.rdc.weflow_server.dto.log.ActivityLogCursorResponseDto;
import com.rdc.weflow_server.dto.log.ActivityLogResponseDto;
import com.rdc.weflow_server.dto.log.ActivityLogStatisticsDto;
import com.rdc.weflow_server.dto.log.CursorDto;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.ActivityLog;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.repository.log.ActivityLogRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import com.rdc.weflow_server.util.CsvGenerator;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ActivityLogRepository activityLogRepository;
    private final CsvGenerator csvGenerator;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    // 로그 생성
    public void createLog(
            ActionType actionType,
            TargetTable targetTable,
            Long targetId,
            Long userId,
            Long projectId,
            String ipAddress
    ) {
        User user = (userId != null) ? userRepository.findById(userId).orElse(null) : null;
        Project project = (projectId != null) ? projectRepository.findById(projectId).orElse(null) : null;

        ActivityLog log = ActivityLog.builder()
                .actionType(actionType)
                .targetTable(targetTable)
                .targetId(targetId)
                .user(user)
                .project(project)
                .ipAddress(ipAddress)
                .build();

        activityLogRepository.save(log);
    }

    // 전체 로그 + 필터링 조회
    public ActivityLogListResponseDto searchLogs(
            String actionType,
            String targetTable,
            Long userId,
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Page<ActivityLogResponseDto> page = activityLogRepository.searchLogs(
                actionType,
                targetTable,
                null,
                userId,
                projectId,
                startDate,
                endDate,
                pageable
        );

        return ActivityLogListResponseDto.builder()
                .logs(page.getContent())
                .totalCount(page.getTotalElements())
                .build();
    }

    public ActivityLogCursorResponseDto searchLogsCursor(
            String actionType,
            String targetTable,
            Long targetId,
            Long userId,
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer limit,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            boolean includeTotal
    ) {
        validateCursorParams(cursorCreatedAt, cursorId);
        int resolvedLimit = resolveLimit(limit);

        boolean firstPage = cursorCreatedAt == null && cursorId == null;
        List<ActivityLogResponseDto> logs = activityLogRepository.searchLogsCursor(
                actionType,
                targetTable,
                targetId,
                userId,
                projectId,
                startDate,
                endDate,
                firstPage ? null : cursorCreatedAt,
                firstPage ? null : cursorId,
                resolvedLimit + 1
        );

        Long totalCount = null;
        if (includeTotal) {
            totalCount = activityLogRepository.countLogsCursor(
                    actionType,
                    targetTable,
                    targetId,
                    userId,
                    projectId,
                    startDate,
                    endDate
            );
            if (totalCount == null) {
                totalCount = 0L;
            }
        }

        boolean hasNext = logs.size() > resolvedLimit;
        if (hasNext) {
            logs = logs.subList(0, resolvedLimit);
        }

        CursorDto nextCursor = null;
        if (hasNext && !logs.isEmpty()) {
            ActivityLogResponseDto last = logs.get(logs.size() - 1);
            nextCursor = CursorDto.builder()
                    .createdAt(last.getCreatedAt())
                    .id(last.getLogId())
                    .build();
        }

        return ActivityLogCursorResponseDto.builder()
                .items(logs)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .totalCount(totalCount)
                .build();
    }

    private void validateCursorParams(LocalDateTime cursorCreatedAt, Long cursorId) {
        boolean hasCreatedAt = cursorCreatedAt != null;
        boolean hasId = cursorId != null;
        if ((hasCreatedAt && !hasId) || (!hasCreatedAt && hasId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "cursorCreatedAt과 cursorId는 함께 전달되어야 합니다.");
        }
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "limit는 1 이상이어야 합니다.");
        }
        return Math.min(limit, MAX_LIMIT);
    }

    // 특정 리소스 로그 조회 (POST/COMMENT/PROJECT...)
    public ActivityLogListResponseDto getLogsByResource(
            String targetTable,
            Long targetId,
            Pageable pageable
    ) {
        Page<ActivityLogResponseDto> page = activityLogRepository.searchByTarget(
                targetTable,
                targetId,
                pageable
        );

        return ActivityLogListResponseDto.builder()
                .logs(page.getContent())
                .totalCount(page.getTotalElements())
                .build();
    }

    // 유저별 로그 조회
    public ActivityLogListResponseDto getLogsByUser(
            Long userId,
            Pageable pageable
    ) {
        Page<ActivityLogResponseDto> page = activityLogRepository.searchByUser(
                userId,
                pageable
        );

        return ActivityLogListResponseDto.builder()
                .logs(page.getContent())
                .totalCount(page.getTotalElements())
                .build();
    }

    // 프로젝트별 로그 조회
    public ActivityLogListResponseDto getLogsByProject(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Page<ActivityLogResponseDto> page = activityLogRepository.searchByProject(
                projectId,
                startDate,
                endDate,
                pageable
        );

        return ActivityLogListResponseDto.builder()
                .logs(page.getContent())
                .totalCount(page.getTotalElements())
                .build();
    }

    // 통계 조회
    public ActivityLogStatisticsDto getStatistics() {
        return activityLogRepository.getStatistics();
    }

    // 삭제 로그 조회
    public ActivityLogListResponseDto getDeletedResourceLogs(
            String targetTable,
            Pageable pageable
    ) {
        Page<ActivityLogResponseDto> page = activityLogRepository.searchDeletedResources(targetTable, pageable);

        return ActivityLogListResponseDto.builder()
                .logs(page.getContent())
                .totalCount(page.getTotalElements())
                .build();
    }

    // 로그 내보내기
    public byte[] exportLogs(
            String actionType,
            String targetTable,
            Long userId,
            Long projectId,
            String startDate,
            String endDate
    ) {
        // 1. 전체 로그 조회
        List<ActivityLogResponseDto> logs =
                activityLogRepository.exportSearch(
                        actionType,
                        targetTable,
                        userId,
                        projectId,
                        startDate,
                        endDate
                );

        // 2. CSV로 변환
        return csvGenerator.generateCsv(logs);
    }
}
