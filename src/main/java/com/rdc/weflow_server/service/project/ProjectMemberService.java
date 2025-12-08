package com.rdc.weflow_server.service.project;

import com.rdc.weflow_server.dto.project.response.ProjectMemberResponse;
import com.rdc.weflow_server.dto.project.response.ProjectRoleUpdateResponse;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.notification.NotificationType;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.project.ProjectRole;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    /** 프로젝트 멤버 조회 **/
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, Long userId) {

        // 프로젝트 멤버인지 검증
        projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_PROJECT_ACCESS));

        // 전체 멤버 반환
        return projectMemberRepository.findAllByProjectId(projectId)
                .stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    /** 프로젝트 멤버 권한 변경 (ADMIN만 가능)**/
    @Transactional
    public ProjectRoleUpdateResponse updateProjectRole(
            Long projectId,
            Long targetMemberId,
            Long requesterId,
            UserRole requesterUserRole,
            String newRole,
            String ip
    ) {

        // (1) SYSTEM_ADMIN은 일반 페이지에서 변경 금지
        if (requesterUserRole == UserRole.SYSTEM_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // (2) 요청자 프로젝트 멤버 검증
        ProjectMember requester = projectMemberRepository
                .findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_PROJECT_ACCESS));

        // (3) 요청자의 프로젝트 Role 확인 → ADMIN만 가능
        if (requester.getRole() != ProjectRole.ADMIN) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_ROLE_INVALID);
        }

        // (4) 대상 멤버 조회
        ProjectMember target = projectMemberRepository.findById(targetMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));

        if (!target.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_PROJECT_ACCESS);
        }

        // (5) newRole 유효성 검증
        ProjectRole newRoleEnum;
        try {
            newRoleEnum = ProjectRole.valueOf(newRole);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_ROLE);
        }

        // (6) 스스로 MEMBER로 강등 금지
        if (target.getUser().getId().equals(requesterId) && newRoleEnum == ProjectRole.MEMBER) {
            throw new BusinessException(ErrorCode.CANNOT_DOWNGRADE_SELF);
        }

        // 기존 역할 저장
        ProjectRole oldRole = target.getRole();

        // (7) 역할 변경 처리
        target.updateRole(newRoleEnum);

        ProjectRoleUpdateResponse response = ProjectRoleUpdateResponse.from(target);

        //  로그 생성
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.PROJECT_MEMBER,
                target.getId(),
                requesterId,
                projectId,
                ip
        );

        // 알림 발송
        String title = "프로젝트 멤버 역할 변경";
        String message = String.format(
                "%s님의 역할이 %s → %s 로 변경되었습니다.",
                target.getUser().getName(),
                oldRole.name(),
                newRoleEnum.name()
        );

        notificationService.send(
                target.getUser(),
                NotificationType.PROJECT_MEMBER_ROLE_UPDATED,
                title,
                message,
                target.getProject(),
                null,
                null
        );

        return response;
    }

    /** 프로젝트 멤버 삭제 (ADMIN만 가능)**/
    @Transactional
    public void removeProjectMember(
            Long projectId,
            Long targetMemberId,
            Long requesterId,
            UserRole requesterUserRole,
            String ip
    ) {

        // (1) SYSTEM_ADMIN은 일반 영역에서 삭제 불가
        if (requesterUserRole == UserRole.SYSTEM_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // (2) 요청자 프로젝트 멤버 검증
        ProjectMember requester = projectMemberRepository
                .findByProjectIdAndUserId(projectId, requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_PROJECT_ACCESS));

        // (3) 프로젝트 내 ADMIN인지 확인
        if (requester.getRole() != ProjectRole.ADMIN) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_REMOVE_FORBIDDEN);
        }

        // (4) 삭제 대상 조회
        ProjectMember target = projectMemberRepository.findById(targetMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));

        if (!target.getProject().getId().equals(projectId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_PROJECT_ACCESS);
        }

        // (5) 자기 자신 삭제 금지
        if (target.getUser().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_REMOVE_FORBIDDEN);
        }

        // (6) Soft delete 적용
        target.softDelete();

        // 로그 기록
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.PROJECT_MEMBER,
                target.getId(),
                requesterId,
                projectId,
                ip
        );

        // 알림 발송
        String title = "프로젝트 멤버 삭제";
        String message = String.format(
                "%s님이 프로젝트에서 제거되었습니다.",
                target.getUser().getName()
        );

        notificationService.send(
                target.getUser(),
                NotificationType.PROJECT_MEMBER_REMOVED,
                title,
                message,
                target.getProject(),
                null,
                null
        );
    }
}
