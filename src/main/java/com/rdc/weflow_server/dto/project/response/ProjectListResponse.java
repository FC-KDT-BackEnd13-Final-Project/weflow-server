package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProjectListResponse {
    private long totalCount;
    private int page;
    private int size;
    private List<ProjectSummaryResponse> projects;

    public static ProjectListResponse of(
            List<ProjectSummaryResponse> projects,
            long totalCount,
            int page,
            int size
    ) {
        return ProjectListResponse.builder()
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .projects(projects)
                .build();
    }
}
