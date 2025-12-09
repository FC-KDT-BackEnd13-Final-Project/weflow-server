package com.rdc.weflow_server.dto.dashboard;

import com.rdc.weflow_server.dto.log.ActivityLogResponseDto;
import com.rdc.weflow_server.dto.step.StepRequestSummaryResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Builder
public class ProjectDashboardResponse {
    private Long projectId;
    private String name;
    private String customerCompanyName;
    private String adminName;
    private String status;

    private int progressPercent;
    private int totalSteps;
    private int completedSteps;
    private String currentStepTitle;
    private String nextApprovalStepTitle;

    private LocalDate endDate;
    private long daysLeft;

    private List<StepRequestSummaryResponse> recentApprovals;
    private List<ActivityLogResponseDto> recentActivities;
}
