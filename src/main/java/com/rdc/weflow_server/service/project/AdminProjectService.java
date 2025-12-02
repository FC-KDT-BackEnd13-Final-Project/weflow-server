package com.rdc.weflow_server.service.project;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.project.*;
import com.rdc.weflow_server.entity.company.Company;
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
    }
}
