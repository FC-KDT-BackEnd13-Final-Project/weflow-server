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
import com.rdc.weflow_server.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<ProjectSummaryResponse> getMyProjects(CustomUserDetails user) {

        UserRole role = user.getRole();

        List<Project> projects;

        switch (role) {

            case SYSTEM_ADMIN -> {
                // 전체 프로젝트 조회
                projects = projectRepository.findAllActiveProjects();
            }

            case AGENCY -> {
                // 전체 프로젝트 조회 (볼 수 있음)
                projects = projectRepository.findAllActiveProjects();
            }

            case CLIENT -> {
                // 본인 프로젝트만 조회
                projects = projectRepository.findActiveProjectsByUser(user.getId());
            }

            default -> throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return projects.stream()
                .map(ProjectSummaryResponse::from)
                .toList();
    }

    // 프로젝트 상세 조회
    public ProjectDetailResponse getProjectDetails(Long projectId, CustomUserDetails user) {

        UserRole role = user.getRole();

        // SYSTEM_ADMIN → 바로 접근 허용
        if (role == UserRole.SYSTEM_ADMIN) {
            Project project = projectRepository.findByIdWithMembersFiltered(projectId, false)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
            return ProjectDetailResponse.from(project);
        }

        // CLIENT / AGENCY (멤버 여부 체크)
        boolean isMember = projectMemberRepository
                .existsByProjectIdAndUserId(projectId, user.getId());

        if (!isMember) {

            if ((role == UserRole.CLIENT) || (role == UserRole.AGENCY)) {
                throw new BusinessException(ErrorCode.NO_PROJECT_PERMISSION);
            }
        }

        Project project = projectRepository.findByIdWithMembersFiltered(projectId, false)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        return ProjectDetailResponse.from(project);
    }
}
