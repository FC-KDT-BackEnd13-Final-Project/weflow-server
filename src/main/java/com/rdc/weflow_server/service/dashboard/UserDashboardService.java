package com.rdc.weflow_server.service.dashboard;

import com.rdc.weflow_server.dto.dashboard.UserDashboardResponse;
import com.rdc.weflow_server.dto.notification.response.NotificationSummaryResponse;
import com.rdc.weflow_server.dto.project.response.ProjectSummaryResponse;
import com.rdc.weflow_server.dto.step.StepRequestSummaryResponse;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.step.StepRequest;
import com.rdc.weflow_server.entity.step.StepRequestStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.notification.NotificationRepository;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.step.StepRequestRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final StepRequestRepository stepRequestRepository;
    private final NotificationRepository notificationRepository;

    public UserDashboardResponse getDashboard(Long userId) {

        // 1) 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserRole userRole = user.getRole();

        // 2) 진행 중인 프로젝트 수
        long inProgressProjectCount =
                projectMemberRepository.countActiveMembershipByUserIdAndProjectStatus(
                        userId,
                        ProjectStatus.IN_PROGRESS
                );

        // 3) 최근 등록된 프로젝트 Top 5
        List<Project> recentProjects =
                projectRepository.findActiveProjectsByUserOrderByCreatedDesc(userId)
                        .stream()
                        .limit(5)
                        .toList();

        List<ProjectSummaryResponse> importantProjects =
                recentProjects.stream()
                        .map(ProjectSummaryResponse::from)
                        .toList();

        // 4) 읽지 않은 알림 수 (삭제된 알림 제외)
        long unreadNotificationCount =
                notificationRepository.countByUserIdAndIsReadFalseAndDeletedAtIsNull(userId);

        // 5) 최근 알림 Top 5 (삭제된 알림 제외)
        List<NotificationSummaryResponse> recentNotifications =
                notificationRepository
                        .findTop5ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                        .stream()
                        .map(NotificationSummaryResponse::from)
                        .toList();

        // 6) 승인 요청 조회 (오직 CLIENT가 승인해야 하는 것만)
        long pendingApprovalCount = 0;
        List<StepRequestSummaryResponse> upcomingApprovals = Collections.emptyList();

        if (userRole == UserRole.CLIENT) {

            // 해당 클라이언트가 속한 프로젝트 목록
            List<Long> projectIds = recentProjects.stream()
                            .map(Project::getId)
                            .toList();

            if (!projectIds.isEmpty()) {

                // 승인 대기 개수
                pendingApprovalCount =
                        stepRequestRepository.countByStep_Project_IdInAndStatus(
                                projectIds,
                                StepRequestStatus.REQUESTED
                        );

                // 승인 대기 Top 5
                List<StepRequest> requests =
                        stepRequestRepository.findTop5ByStep_Project_IdInAndStatusOrderByCreatedAtDesc(
                                projectIds,
                                StepRequestStatus.REQUESTED
                        );

                upcomingApprovals =
                        requests.stream()
                                .map(StepRequestSummaryResponse::from)
                                .toList();
            }
        }

        // 최종 Response
        return UserDashboardResponse.builder()
                .inProgressProjectCount(inProgressProjectCount)
                .unreadNotificationCount(unreadNotificationCount)
                .recentNotifications(recentNotifications)
                .pendingApprovalCount(pendingApprovalCount)
                .importantProjects(importantProjects)
                .upcomingApprovals(upcomingApprovals)
                .build();
    }
}