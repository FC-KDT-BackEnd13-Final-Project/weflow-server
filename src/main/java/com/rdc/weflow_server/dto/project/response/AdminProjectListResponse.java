package com.rdc.weflow_server.dto.project.response;

import com.rdc.weflow_server.entity.project.Project;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminProjectListResponse {
    private long totalCount;
    private int page;
    private int size;
    private List<AdminProjectSummary> projects;

    public static AdminProjectListResponse of(
            List<Project> projects,
            long totalCount,
            int page,
            int size
    ) {
        return AdminProjectListResponse.builder()
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .projects(
                        projects.stream()
                                .map(AdminProjectSummary::from)
                                .toList()
                )
                .build();
    }
}
