package com.rdc.weflow_server.controller.log;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.log.ActivityLogListResponseDto;
import com.rdc.weflow_server.dto.log.ActivityLogStatisticsDto;
import com.rdc.weflow_server.service.log.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class AdminActivityLogController {

    private final ActivityLogService activityLogService;

    // 전체 로그 조회 + 필터링
    @GetMapping
    public ApiResponse<ActivityLogListResponseDto> getLogs(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetTable,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable
    ) {
        ActivityLogListResponseDto data = activityLogService.searchLogs(
                actionType,
                targetTable,
                userId,
                projectId,
                startDate,
                endDate,
                pageable
        );

        return ApiResponse.success("LOGS_FETCHED", data);
    }

    // 특정 리소스 로그 조회
    @GetMapping("/resources/{table}/{id}")
    public ApiResponse<ActivityLogListResponseDto> getResourceLogs(
            @PathVariable String table,
            @PathVariable Long id,
            Pageable pageable
    ) {
        ActivityLogListResponseDto data = activityLogService.getLogsByResource(
                table,
                id,
                pageable
        );

        return ApiResponse.success("RESOURCE_LOGS_FETCHED", data);
    }

    //  특정 유저 로그 조회
    @GetMapping("/users/{userId}")
    public ApiResponse<ActivityLogListResponseDto> getUserLogs(
            @PathVariable Long userId,
            Pageable pageable
    ) {
        ActivityLogListResponseDto data = activityLogService.getLogsByUser(userId, pageable);

        return ApiResponse.success("USER_LOGS_FETCHED", data);
    }

    // 특정 프로젝트 로그 조회
    @GetMapping("/projects/{projectId}")
    public ApiResponse<ActivityLogListResponseDto> getProjectLogs(
            @PathVariable Long projectId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable
    ) {
        ActivityLogListResponseDto data = activityLogService.getLogsByProject(projectId, startDate, endDate, pageable);

        return ApiResponse.success("PROJECT_LOGS_FETCHED", data);
    }

    // 삭제된 항목 로그 조회
    @GetMapping("/deleted/{table}")
    public ApiResponse<ActivityLogListResponseDto> getDeletedLogs(
            @PathVariable String table,
            Pageable pageable
    ) {
        ActivityLogListResponseDto data = activityLogService.getDeletedResourceLogs(
                table,
                pageable
        );

        return ApiResponse.success("DELETED_RESOURCE_LOGS_FETCHED", data);
    }

    // 로그 통계 조회
    @GetMapping("/statistics")
    public ApiResponse<ActivityLogStatisticsDto> getStatistics() {
        ActivityLogStatisticsDto data = activityLogService.getStatistics();
        return ApiResponse.success("LOG_STATISTICS_FETCHED", data);
    }

    // 로그 내보내기
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetTable,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        byte[] fileData = activityLogService.exportLogs(
                actionType,
                targetTable,
                userId,
                projectId,
                startDate,
                endDate
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logs_export.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileData);
    }
}
