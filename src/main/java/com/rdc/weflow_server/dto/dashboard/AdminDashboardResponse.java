package com.rdc.weflow_server.dto.dashboard;

import com.rdc.weflow_server.dto.log.ActivityLogResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminDashboardResponse {
    private long totalUsers;          // 전체 회원 수
    private long totalCompanies;      // 전체 회사 수
    private long totalProjects;       // 전체 프로젝트 수

    private List<ActivityLogResponseDto> recentLogs; // 최근 활동 로그 (5개)
}