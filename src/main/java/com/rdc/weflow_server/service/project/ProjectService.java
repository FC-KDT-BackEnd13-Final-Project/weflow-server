package com.rdc.weflow_server.service.project;

import com.rdc.weflow_server.config.security.CustomUserDetails;
import com.rdc.weflow_server.dto.project.response.ProjectDetailResponse;
import com.rdc.weflow_server.dto.project.response.ProjectListResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectListResponse getMyProjects(
            CustomUserDetails user,
            String keyword,
            int page,
            int size
    ) {
        Long userId = user.getId();
        UserRole role = user.getRole();

        List<ProjectSummaryResponse> projects =
                projectRepository.searchMyProjects(userId, role, keyword, page, size);

        long total =
                projectRepository.countMyProjects(userId, role, keyword);

        return ProjectListResponse.of(projects, total, page, size);
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
