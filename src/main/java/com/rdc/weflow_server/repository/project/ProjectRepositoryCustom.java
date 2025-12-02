package com.rdc.weflow_server.repository.project;

import com.rdc.weflow_server.entity.project.Project;
import com.rdc.weflow_server.entity.project.ProjectStatus;

import java.util.List;

public interface ProjectRepositoryCustom {
    List<Project> searchAdminProjects(
            ProjectStatus status,
            Long companyId,
            String keyword,
            int page,
            int size
    );

    long countAdminProjects(
            ProjectStatus status,
            Long companyId,
            String keyword
    );
}
