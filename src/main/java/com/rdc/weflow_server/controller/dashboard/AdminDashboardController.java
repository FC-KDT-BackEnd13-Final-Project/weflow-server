package com.rdc.weflow_server.controller.dashboard;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.dto.dashboard.AdminDashboardResponse;
import com.rdc.weflow_server.service.dashboard.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ApiResponse<AdminDashboardResponse> getAdminDashboard() {
        AdminDashboardResponse data = adminDashboardService.getDashboard();
        return ApiResponse.success("ADMIN_DASHBOARD_FETCHED", data);
    }
}
