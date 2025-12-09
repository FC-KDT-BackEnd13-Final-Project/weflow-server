package com.rdc.weflow_server.controller.dashboard;

import com.rdc.weflow_server.common.api.ApiResponse;
import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.dashboard.UserDashboardResponse;
import com.rdc.weflow_server.service.dashboard.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class UserDashboardController {
    private final UserDashboardService userDashboardService;

    // 사용자 본인 대시보드 조회
    @GetMapping("/me")
    public ApiResponse<UserDashboardResponse> getMyDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }
        Long userId = userDetails.getId();
        UserDashboardResponse data = userDashboardService.getDashboard(userId);

        return ApiResponse.success("USER_DASHBOARD_FETCHED", data);
    }
}
