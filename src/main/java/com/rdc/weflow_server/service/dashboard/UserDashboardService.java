package com.rdc.weflow_server.service.dashboard;

import com.rdc.weflow_server.dto.dashboard.UserDashboardResponse;
import com.rdc.weflow_server.dto.notification.response.NotificationSummaryResponse;
import com.rdc.weflow_server.dto.project.response.ProjectSummaryResponse;
import com.rdc.weflow_server.dto.step.StepRequestSummaryResponse;
import com.rdc.weflow_server.entity.project.Project;
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
import java.util.Optional;

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
                projectMemberRepository.countByUserIdAndProject_StatusNot(
                        userId,
                        ProjectStatus.CLOSED
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

        UpcomingStepRequestScope scope = buildUpcomingScope(user);
        if (!scope.isEmpty()) {
            pendingApprovalCount = countUpcomingApprovals(scope);
            upcomingApprovals = findUpcomingApprovals(scope).stream()
                    .map(StepRequestSummaryResponse::from)
                    .toList();
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

    private UpcomingStepRequestScope buildUpcomingScope(User user) {
        if (isSystemAdmin(user)) {
            return UpcomingStepRequestScope.admin(List.of(
                    StepRequestStatus.REQUESTED,
                    StepRequestStatus.CHANGE_REQUESTED
            ));
        }

        if (isClient(user)) {
            List<Long> projectIds = projectMemberRepository.findActiveProjectIdsByUserId(user.getId());
            return UpcomingStepRequestScope.client(
                    projectIds,
                    List.of(StepRequestStatus.REQUESTED)
            );
        }

        if (isAgency(user)) {
            return UpcomingStepRequestScope.agency(
                    user.getId(),
                    List.of(StepRequestStatus.CHANGE_REQUESTED)
            );
        }

        return UpcomingStepRequestScope.empty();
    }

    private long countUpcomingApprovals(UpcomingStepRequestScope scope) {
        return switch (scope.type()) {
            case ADMIN -> stepRequestRepository.countByStatusIn(scope.statuses());
            case CLIENT -> scope.projectIds().isEmpty()
                    ? 0
                    : stepRequestRepository.countByStep_Project_IdInAndStatusIn(scope.projectIds(), scope.statuses());
            case AGENCY -> scope.requesterId()
                    .map(requesterId -> stepRequestRepository.countByRequestedBy_IdAndStatusIn(
                            requesterId,
                            scope.statuses()
                    ))
                    .orElse(0L);
            case EMPTY -> 0;
        };
    }

    private List<StepRequest> findUpcomingApprovals(UpcomingStepRequestScope scope) {
        return switch (scope.type()) {
            case ADMIN -> stepRequestRepository.findTop5ByStatusInOrderByCreatedAtDesc(scope.statuses());
            case CLIENT -> scope.projectIds().isEmpty()
                    ? List.of()
                    : stepRequestRepository.findTop5ByStep_Project_IdInAndStatusInOrderByCreatedAtDesc(
                            scope.projectIds(),
                            scope.statuses()
                    );
            case AGENCY -> scope.requesterId()
                    .map(requesterId -> stepRequestRepository.findTop5ByRequestedBy_IdAndStatusInOrderByCreatedAtDesc(
                            requesterId,
                            scope.statuses()
                    ))
                    .orElse(List.of());
            case EMPTY -> List.of();
        };
    }

    private boolean isSystemAdmin(User user) {
        return user != null && user.getRole() == UserRole.SYSTEM_ADMIN;
    }

    private boolean isClient(User user) {
        return user != null && user.getRole() == UserRole.CLIENT;
    }

    private boolean isAgency(User user) {
        return user != null && user.getRole() == UserRole.AGENCY;
    }

    private record UpcomingStepRequestScope(
            ScopeType type,
            List<Long> projectIds,
            Optional<Long> requesterId,
            List<StepRequestStatus> statuses
    ) {
        static UpcomingStepRequestScope admin(List<StepRequestStatus> statuses) {
            return new UpcomingStepRequestScope(ScopeType.ADMIN, List.of(), Optional.empty(), statuses);
        }

        static UpcomingStepRequestScope client(List<Long> projectIds, List<StepRequestStatus> statuses) {
            return new UpcomingStepRequestScope(ScopeType.CLIENT, projectIds, Optional.empty(), statuses);
        }

        static UpcomingStepRequestScope agency(Long requesterId, List<StepRequestStatus> statuses) {
            return new UpcomingStepRequestScope(ScopeType.AGENCY, List.of(), Optional.ofNullable(requesterId), statuses);
        }

        static UpcomingStepRequestScope empty() {
            return new UpcomingStepRequestScope(ScopeType.EMPTY, List.of(), Optional.empty(), List.of());
        }

        boolean isEmpty() {
            return type == ScopeType.EMPTY;
        }
    }

    private enum ScopeType {
        ADMIN,
        CLIENT,
        AGENCY,
        EMPTY
    }
}
