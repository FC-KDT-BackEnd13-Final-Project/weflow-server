package com.rdc.weflow_server.repository.log;

import com.rdc.weflow_server.dto.log.ActivityLogResponseDto;
import com.rdc.weflow_server.dto.log.ActivityLogStatisticsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepositoryCustom {

    Page<ActivityLogResponseDto> searchLogs(
            String actionType,
            String targetTable,
            Long userId,
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<ActivityLogResponseDto> searchByTarget(
            String targetTable,
            Long targetId,
            Pageable pageable
    );

    Page<ActivityLogResponseDto> searchByUser(
            Long userId,
            Pageable pageable
    );

    Page<ActivityLogResponseDto> searchByProject(
            Long projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
    ActivityLogStatisticsDto getStatistics(); // 로그 통계 조회
    Page<ActivityLogResponseDto> searchDeletedResources(String targetTable, Pageable pageable); // 삭제 로그 조회
    List<ActivityLogResponseDto> exportSearch( // 로그 내보내기
            String actionType,
            String targetTable,
            Long userId,
            Long projectId,
            String startDate,
            String endDate
    );
    List<ActivityLogResponseDto> findRecentLogs(int limit); // 최근 로그 5개
}
