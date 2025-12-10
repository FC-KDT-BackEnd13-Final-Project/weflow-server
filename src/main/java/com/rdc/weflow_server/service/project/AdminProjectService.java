package com.rdc.weflow_server.service.project;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.project.request.AdminProjectCreateRequest;
import com.rdc.weflow_server.dto.project.request.AdminProjectMemberAddRequest;
import com.rdc.weflow_server.dto.project.request.AdminProjectUpdateRequest;
import com.rdc.weflow_server.dto.project.response.*;
import com.rdc.weflow_server.dto.step.StepCreateRequest;
import com.rdc.weflow_server.entity.company.Company;
import com.rdc.weflow_server.entity.log.ActionType;
import com.rdc.weflow_server.entity.log.TargetTable;
import com.rdc.weflow_server.entity.notification.NotificationType;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.project.ProjectRole;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.user.User;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.company.CompanyRepository;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import com.rdc.weflow_server.repository.user.UserRepository;
import com.rdc.weflow_server.service.log.ActivityLogService;
import com.rdc.weflow_server.service.log.AuditContext;
import com.rdc.weflow_server.service.notification.NotificationService;
import com.rdc.weflow_server.service.step.StepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final StepService stepService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    // 관리자 체크 공통 메소드
    private static void validateAdmin(CustomUserDetails user) {
        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    // 프로젝트 생성
    public AdminProjectCreateResponse createProject(
            AdminProjectCreateRequest request,
            CustomUserDetails user,
            String ip
    ) {
        // 관리자 체크
        validateAdmin(user);

        // 회사 조회
        Company company = companyRepository.findById(request.getCustomerCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_COMPANY_NOT_FOUND));

        // 프로젝트 등록한 시스템 관리자 조회
        Long creatorId = user.getId();
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Project project = request.toEntity(company, creatorId);
        projectRepository.save(project);

        // 단계 생성 로직
        if (request.getSteps() != null && !request.getSteps().isEmpty()) {

            // 프론트에서 전달된 커스텀 단계들을 그대로 생성
            for (StepCreateRequest stepReq : request.getSteps()) {
                AuditContext ctx = new AuditContext(creatorId, ip, project.getId());
                stepService.createStep(project.getId(), stepReq, ctx);
            }

        } else {

            // 단계가 하나도 전달되지 않은 경우 → 기본 단계 자동 생성
            stepService.createDefaultStepsForProject(project, creator);
        }

        // 로그 기록
        activityLogService.createLog(
                ActionType.CREATE,
                TargetTable.PROJECT,
                project.getId(),
                creatorId,
                project.getId(),
                ip
        );

        // 프로젝트 생성 후 알림
        notificationService.send(
                creator,   // 생성한 관리자
                NotificationType.PROJECT_CREATED,
                "프로젝트가 생성되었습니다",
                String.format("[%s] 프로젝트가 생성되었습니다.", project.getName()),
                project,
                null,
                null
        );

        return AdminProjectCreateResponse.from(project);
    }

    // 프로젝트 목록 조회
    public AdminProjectListResponse getProjectList(
            ProjectStatus status,
            Long companyId,
            String keyword,
            int page,
            int size
    ) {
        List<Project> projects = projectRepository.searchAdminProjects(
                status, companyId, keyword, page, size
        );

        long total = projectRepository.countAdminProjects(status, companyId, keyword);

        return AdminProjectListResponse.of(projects, total, page, size);
    }

    // 프로젝트 상세 조회
    public AdminProjectDetailResponse getProjectDetail(Long projectId) {

        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        return AdminProjectDetailResponse.from(project);
    }

    // 프로젝트 수정
    public AdminProjectUpdateResponse updateProject(
            Long projectId,
            AdminProjectUpdateRequest request,
            CustomUserDetails user,
            String ip
    ) {
        // 관리자 체크
        validateAdmin(user);

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 상태 변경 여부 비교를 위해 기존 상태 저장
        ProjectStatus oldStatus = project.getStatus();

        // 회사 변경 필요할 경우
        Company company = null;
        if (request.getCustomerCompanyId() != null) {
            company = companyRepository.findById(request.getCustomerCompanyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_COMPANY_NOT_FOUND));
        }

        // 업데이트
        project.updateProject(
                request.getName(),
                request.getDescription(),
                request.getStatus(),
                request.getStartDate(),
                request.getEndDateExpected(),
                request.getEndDate(),
                request.getContractAmount() != null ? BigDecimal.valueOf(request.getContractAmount()) : null,
                request.getContractFileUrl(),
                company
        );

        projectRepository.save(project);

        // 로그 기록
        activityLogService.createLog(
                ActionType.UPDATE,
                TargetTable.PROJECT,
                project.getId(),
                user.getId(),
                projectId,
                ip
        );

        // 프로젝트 정보/상태 변경 알림
        // 프로젝트 멤버들 조회
        List<ProjectMember> members = projectMemberRepository
                .findByProjectIdAndDeletedAtIsNull(projectId);

        // 어떤 타입의 알림을 보낼지 결정
        NotificationType type;

        // 상태가 변경되었을 때
        if (request.getStatus() != null && oldStatus != request.getStatus()) {

            // 완료 상태로 바뀌었으면 PROJECT_COMPLETED
            if (request.getStatus() == ProjectStatus.CLOSED) {
                type = NotificationType.PROJECT_COMPLETED;
            } else {
                type = NotificationType.PROJECT_STATUS_CHANGED;
            }

        } else {
            // 상태 변경이 아니면 정보 변경
            type = NotificationType.PROJECT_INFO_UPDATED;
        }

        // 모든 멤버에게 알림 발송
        for (ProjectMember pm : members) {
            notificationService.send(
                    pm.getUser(),
                    type,
                    "프로젝트 정보 변경",
                    String.format("[%s] 프로젝트 정보가 변경되었습니다.", project.getName()),
                    project,
                    null,
                    null
            );
        }

        return new AdminProjectUpdateResponse(
                project.getId(),
                project.getUpdatedAt().toString()
        );
    }

    // 프로젝트 삭제
    public void deleteProject(
            Long projectId,
            CustomUserDetails user,
            String ip
    ) {

        // 관리자만 삭제 가능
        validateAdmin(user);

        Project project = projectRepository.findByIdWithMembersFiltered(projectId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // Soft Delete
        project.softDelete();

        projectRepository.save(project);

        // 로그 기록
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.PROJECT,
                projectId,
                user.getId(),
                projectId,
                ip
        );
    }

    // 프로젝트 멤버 추가
    public AdminProjectMemberAddResponse addProjectMember(
            Long projectId,
            AdminProjectMemberAddRequest request,
            CustomUserDetails user,
            String ip
    ) {

        // 1) 관리자만 가능
        validateAdmin(user);

        // 2) 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 3) 유저 조회
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 4) 이미 멤버인지 확인
        boolean exists = project.getProjectMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(request.getUserId()));

        if (exists) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_ALREADY_EXISTS);
        }

        // 5) 멤버 추가
        ProjectMember member = ProjectMember.create(
                project,
                targetUser,
                ProjectRole.valueOf(request.getProjectRole())
        );

        projectMemberRepository.save(member);

        // 로그 기록
        activityLogService.createLog(
                ActionType.CREATE,
                TargetTable.PROJECT_MEMBER,
                member.getId(),
                user.getId(),
                project.getId(),
                ip
        );

        // 알림 발송
        notificationService.send(
                targetUser,
                NotificationType.PROJECT_MEMBER_ADDED,
                "프로젝트에 초대되었습니다",
                String.format("[%s] 프로젝트에 참여하게 되었습니다.", project.getName()),
                project,
                null,
                null
        );

        return AdminProjectMemberAddResponse.of(targetUser.getId(), request.getProjectRole());
    }

    // 프로젝트 멤버 조회
    public AdminProjectMemberListResponse getProjectMembers(
            Long projectId,
            CustomUserDetails user
    ) {
        // 1) 관리자만 조회
        validateAdmin(user);

        // 2) 프로젝트 존재 여부 확인
        projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 3) 멤버 목록 조회 (삭제된 멤버도 포함)
        List<ProjectMember> members = projectMemberRepository.findAllByProjectIdIncludeDeleted(projectId);

        return AdminProjectMemberListResponse.of(members);
    }

    // 프로젝트 멤버 삭제
    public void removeProjectMember(
            Long projectId,
            Long userId,
            CustomUserDetails user,
            String ip
    ) {

        // 관리자 체크
        validateAdmin(user);

        // 1) 프로젝트 존재 여부 확인
        projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 2) 멤버 조회 (삭제 포함)
        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_MEMBER_NOT_FOUND));

        // 3) 이미 삭제된 멤버인지 확인
        if (member.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.PROJECT_MEMBER_ALREADY_REMOVED);
        }

        // 4) Soft Delete
        member.softDelete();
        projectMemberRepository.save(member);

        // 로그 기록
        activityLogService.createLog(
                ActionType.DELETE,
                TargetTable.PROJECT_MEMBER,
                member.getId(),
                user.getId(),
                projectId,
                ip
        );

        // 알림 발송
        notificationService.send(
                member.getUser(),
                NotificationType.PROJECT_MEMBER_REMOVED,
                "프로젝트에서 제외되었습니다",
                String.format("[%s] 프로젝트에서 제외되었습니다.", member.getProject().getName()),
                member.getProject(),
                null,
                null
        );
    }
}
