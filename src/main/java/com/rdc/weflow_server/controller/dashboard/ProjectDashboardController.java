package com.rdc.weflow_server.controller.dashboard;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.dashboard.ProjectDashboardResponse;
import com.rdc.weflow_server.service.dashboard.ProjectDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectDashboardController {
    private final ProjectDashboardService projectDashboardService;

    // 프로젝트 대시보드 조회
    @GetMapping("/{projectId}/dashboard")
    public ApiResponse<ProjectDashboardResponse> getProjectDashboard(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ProjectDashboardResponse response =
                projectDashboardService.getDashboard(projectId);

        return ApiResponse.success("PROJECT_DASHBOARD_FETCHED", response);
    }
}
