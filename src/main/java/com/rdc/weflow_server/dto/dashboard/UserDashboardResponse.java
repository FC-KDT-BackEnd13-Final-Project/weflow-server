package com.rdc.weflow_server.dto.dashboard;

import com.rdc.weflow_server.dto.notification.response.NotificationSummaryResponse;
import com.rdc.weflow_server.dto.project.response.ProjectSummaryResponse;
import com.rdc.weflow_server.dto.step.StepRequestSummaryResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserDashboardResponse {
    // 1) 숫자 지표
    private long inProgressProjectCount;   // 진행중인 프로젝트 수
    private long pendingApprovalCount;     // 처리해야 하는 승인 요청 수
    private long unreadNotificationCount;  // 읽지 않은 알림 수

    // 2) 중요 프로젝트
    private List<ProjectSummaryResponse> importantProjects;

    // 3) 다가오는 승인 요청 목록
    private List<StepRequestSummaryResponse> upcomingApprovals;

    //  4) 최근 알림
    private List<NotificationSummaryResponse> recentNotifications;
}
