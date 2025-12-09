package com.rdc.weflow_server.service.dashboard;

import com.rdc.weflow_server.dto.dashboard.ProjectDashboardResponse;
import com.rdc.weflow_server.dto.log.ActivityLogResponseDto;
import com.rdc.weflow_server.dto.step.StepRequestSummaryResponse;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.step.Step;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.repository.log.ActivityLogRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.step.StepRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectDashboardService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final StepRepository stepRepository;
    private final StepRequestRepository stepRequestRepository;
    private final ActivityLogRepository activityLogRepository;

    public ProjectDashboardResponse getDashboard(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        // 관리자 이름
        User admin = userRepository.findById(project.getCreatedBy()).orElse(null);

        // 고객사 이름
        String customerCompanyName = project.getCompany() != null
                ? project.getCompany().getName()
                : null;

        // ----- Step 정보 -----
        List<Step> steps = stepRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        int totalSteps = steps.size();

        int completedSteps = 0;
        for (Step step : steps) {
            boolean hasApprovedRequest =
                    stepRequestRepository.existsByStepIdAndStatus(step.getId(), StepRequestStatus.APPROVED);

            if (hasApprovedRequest) completedSteps++;
        }

        // 진행률 계산
        int progressPercent = (totalSteps == 0) ? 0 : (completedSteps * 100 / totalSteps);

        // 현재 단계 (완료된 다음 단계)
        String currentStepTitle = null;
        if (completedSteps < totalSteps) {
            currentStepTitle = steps.get(completedSteps).getTitle();
        }

        // 다음 승인 대상 단계
        String nextApprovalStepTitle =
                stepRequestRepository
                        .findFirstByStep_Project_IdAndStatusOrderByCreatedAtAsc(projectId, StepRequestStatus.REQUESTED)
                        .map(req -> req.getStep().getTitle())
                        .orElse(null);

        // 최근 승인 요청 TOP 3
        List<StepRequestSummaryResponse> recentApprovals =
                stepRequestRepository.findTop3ByStep_Project_IdOrderByCreatedAtDesc(projectId)
                        .stream()
                        .map(StepRequestSummaryResponse::from)
                        .toList();

        // 최근 활동 로그 TOP 5
        List<ActivityLogResponseDto> recentActivities =
                activityLogRepository.findTop5ByProjectIdOrderByCreatedAtDesc(projectId)
                        .stream()
                        .map(ActivityLogResponseDto::from)
                        .toList();

        // ----- 종료일 / 남은 기간 -----
        LocalDate endDate = null;
        LocalDateTime deadline = project.getExpectedEndDate();

        if (deadline != null) {
            endDate = deadline.toLocalDate();
        }

        long daysLeft = 0;
        if (endDate != null) {
            long diff = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
            daysLeft = Math.max(diff, 0);
        }

        return ProjectDashboardResponse.builder()
                .projectId(project.getId())
                .name(project.getName())
                .customerCompanyName(customerCompanyName)
                .adminName(admin != null ? admin.getName() : null)
                .status(String.valueOf(project.getStatus()))

                .progressPercent(progressPercent)
                .totalSteps(totalSteps)
                .completedSteps(completedSteps)
                .currentStepTitle(currentStepTitle)
                .nextApprovalStepTitle(nextApprovalStepTitle)

                .endDate(endDate)
                .daysLeft(daysLeft)

                .recentApprovals(recentApprovals)
                .recentActivities(recentActivities)
                .build();
    }
}