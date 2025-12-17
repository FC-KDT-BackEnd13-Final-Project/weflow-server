package com.rdc.weflow_server.repository.project;

import com.rdc.weflow_server.dto.project.response.ProjectSummaryResponse;
import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectPhase;
import com.rdc.weflow_server.entity.project.ProjectStatus;
import com.rdc.weflow_server.entity.user.UserRole;

import java.util.List;

public interface ProjectRepositoryCustom {
    List<Project> searchAdminProjects(
            ProjectPhase phase,
            ProjectStatus status,
            Long companyId,
            String keyword,
            int page,
            int size
    );

    long countAdminProjects(
            ProjectPhase phase,
            ProjectStatus status,
            Long companyId,
            String keyword
    );

    List<ProjectSummaryResponse> searchMyProjects(
            Long userId,
            UserRole role,
            String keyword,
            int page,
            int size
    );

    long countMyProjects(
            Long userId,
            UserRole role,
            String keyword
    );
}
