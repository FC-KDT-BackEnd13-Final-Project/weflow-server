package com.rdc.weflow_server.service.project;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.project.response.ProjectDetailResponse;
import com.rdc.weflow_server.dto.project.response.ProjectSummaryResponse;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectMember;
import com.rdc.weflow_server.entity.user.UserRole;
import com.rdc.weflow_server.exception.BusinessException;
import com.rdc.weflow_server.exception.ErrorCode;
import com.rdc.weflow_server.repository.project.ProjectMemberRepository;
import com.rdc.weflow_server.repository.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // 내 프로젝트 조회
    public List<ProjectSummaryResponse> getMyProjects(CustomUserDetails user) {

        boolean includeDeleted = user.getRole() == UserRole.SYSTEM_ADMIN;

        // 1) 유저가 속한 모든 프로젝트 목록 조회
        List<ProjectMember> memberships =
                projectMemberRepository.findByUserIdFiltered(user.getId(), includeDeleted);

        // 2) 프로젝트 정보로 매핑
        return memberships.stream()
                .map(ProjectSummaryResponse::from)
                .toList();
    }

    // 프로젝트 상세 조회
    public ProjectDetailResponse getProjectDetails(Long projectId, CustomUserDetails user) {

        boolean includeDeleted = user.getRole() == UserRole.SYSTEM_ADMIN;

        // 권한 체크 (해당 프로젝트의 멤버인지 확인)
        checkProjectAccess(projectId, user);

        Project project = projectRepository.findByIdWithMembersFiltered(projectId, includeDeleted)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        return ProjectDetailResponse.from(project);
    }

    // 공통 권한 체크 메서드
    private void checkProjectAccess(Long projectId, CustomUserDetails user) {

        // 시스템 관리자면 무조건 허용
        if (user.getRole() == UserRole.SYSTEM_ADMIN) return;

        boolean isMember =
                projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());

        if (!isMember) {
            throw new BusinessException(ErrorCode.NO_PROJECT_PERMISSION);
        }
    }
}
