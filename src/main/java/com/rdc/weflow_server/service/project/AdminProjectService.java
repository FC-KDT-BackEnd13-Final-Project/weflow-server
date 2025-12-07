package com.rdc.weflow_server.service.project;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.project.*;
import com.rdc.weflow_server.entity.company.Company;
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

    // 관리자 체크 공통 메소드
    private static void validateAdmin(CustomUserDetails user) {
        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    // 프로젝트 생성
    public AdminProjectCreateResponseDto createProject(
            AdminProjectCreateRequestDto request,
            CustomUserDetails user
    ) {
        // 관리자 체크
        validateAdmin(user);

        // 회사 조회
        Company company = companyRepository.findById(request.getCustomerCompanyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_COMPANY_NOT_FOUND));

        // 프로젝트 등록한 시스템 관리자 조회
        Long creatorId = user.getId();

        Project project = request.toEntity(company, creatorId);
        projectRepository.save(project);

        // 프로젝트 생성 시 기본 단계 자동 생성 (IN_PROGRESS 상위 흐름 하에 카테고리 순서대로)
        User creator = userRepository.findById(creatorId).orElse(null);
        stepService.createDefaultStepsForProject(project, creator);

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

        return AdminProjectCreateResponseDto.from(project);
    }

    // 프로젝트 목록 조회
    public AdminProjectListResponseDto getProjectList(
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

        return AdminProjectListResponseDto.of(projects, total, page, size);
    }

    // 프로젝트 상세 조회
    public AdminProjectDetailResponseDto getProjectDetail(Long projectId) {

        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        return AdminProjectDetailResponseDto.from(project);
    }

    // 프로젝트 수정
    public AdminProjectUpdateResponseDto updateProject(
            Long projectId,
            AdminProjectUpdateRequestDto request,
            CustomUserDetails user
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

        return new AdminProjectUpdateResponseDto(
                project.getId(),
                project.getUpdatedAt().toString()
        );
    }

    // 프로젝트 삭제
    public void deleteProject(Long projectId, CustomUserDetails user) {

        // 관리자만 삭제 가능
        validateAdmin(user);

        Project project = projectRepository.findByIdWithMembersFiltered(projectId, true)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // Soft Delete
        project.softDelete();

        projectRepository.save(project);
    }

    // 프로젝트 멤버 추가
    public AdminProjectMemberAddResponseDto addProjectMember(Long projectId, AdminProjectMemberAddRequestDto request, CustomUserDetails user) {

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

        return AdminProjectMemberAddResponseDto.of(targetUser.getId(), request.getProjectRole());
    }

    // 프로젝트 멤버 조회
    public AdminProjectMemberListResponseDto getProjectMembers(
            Long projectId,
            CustomUserDetails user
    ) {
        // 1) 관리자만 조회
        validateAdmin(user);

        // 2) 프로젝트 존재 여부 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 3) 멤버 목록 조회 (삭제된 멤버도 포함)
        List<ProjectMember> members = projectMemberRepository.findAllByProjectIdIncludeDeleted(projectId);

        return AdminProjectMemberListResponseDto.of(members);
    }

    // 프로젝트 멤버 삭제
    public void removeProjectMember(Long projectId, Long userId, CustomUserDetails user) {

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
